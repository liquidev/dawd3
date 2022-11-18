use std::cell::RefCell;
use std::fmt::Display;
use std::fs::File;

use cpal::traits::{DeviceTrait, HostTrait, StreamTrait};
use cpal::{BufferSize, Device, Host, SampleRate, Stream, StreamConfig};
use hound::{SampleFormat, WavSpec};
use jni::objects::{JClass, JMethodID, JObject};
use jni::signature::ReturnType;
use jni::sys::jvalue;
use jni::JNIEnv;
use log::{error, info, warn, LevelFilter};

use crate::registry::Registry;

mod registry;

struct GlobalState {
    host: Option<Host>,
    devices: Registry<Device>,
    streams: Registry<Stream>,
}

thread_local! {
    static STATE: RefCell<GlobalState> = RefCell::new(GlobalState {
        host: None,
        devices: Registry::new(),
        streams: Registry::new(),
    });
}

fn with_global_state<T>(f: impl FnOnce(&mut GlobalState) -> T) -> T {
    STATE.with(|state| {
        let mut state = state.borrow_mut();
        f(&mut state)
    })
}

fn try_with_global_state<T, E>(env: JNIEnv, f: impl FnOnce(&mut GlobalState) -> Result<T, E>) -> T
where
    T: Default,
    E: Display,
{
    let result = with_global_state(|state| f(state));
    match result {
        Ok(value) => value,
        Err(error) => {
            let _ = env.throw_new("net/liquidev/d3r/D3rException", error.to_string());
            T::default()
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_net_liquidev_d3r_D3r_initialize(_env: JNIEnv, _: JClass) {
    env_logger::builder()
        .filter(Some("dawd3_d3r"), LevelFilter::Trace)
        .format_timestamp(None)
        .init();
    info!("d3r initialized successfully")
}

#[no_mangle]
pub extern "system" fn Java_net_liquidev_d3r_D3r_openDefaultHost(env: JNIEnv, _: JClass) {
    try_with_global_state(env, |state| {
        if state.host.is_some() {
            Err("Audio host already open")
        } else {
            let host = cpal::default_host();
            info!("using host: {:?}", host.id());
            state.host = Some(host);
            Ok(())
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_net_liquidev_d3r_D3r_openDefaultOutputDevice(
    env: JNIEnv,
    _: JClass,
) -> u32 {
    try_with_global_state(env, |state| {
        let Some(host) = &state.host else { return Err("Host is not open"); };
        let Some(device) = host.default_output_device() else { return Err("No default output device found"); };
        if let Ok(name) = device.name() {
            info!("default output device opened successfully: {name}");
        } else {
            warn!("default output device opened successfully, but could not obtain its name");
        }
        Ok(state.devices.add(device))
    })
}

fn generate_audio(
    output: &mut [f32],
    config: &StreamConfig,
    env: JNIEnv,
    generator: JObject,
    method: JMethodID,
) -> Result<(), String> {
    let buffer = env
        .call_method_unchecked(
            generator,
            method,
            ReturnType::Array,
            &[
                jvalue {
                    i: output.len() as i32,
                },
                jvalue {
                    i: config.channels as i32,
                },
            ],
        )
        .map_err(|e| format!("error while calling into the audio generator: {e}"))?;
    let buffer = buffer.l().expect("buffer must be an object");
    let len = env
        .get_array_length(buffer.into_raw())
        .expect("buffer must be a float[]");
    if (len as usize) < output.len() {
        return Err(format!(
            "audio buffer length is too short (expected {}, but generator returned {len})",
            output.len()
        ));
    }
    env.get_float_array_region(buffer.into_raw(), 0, output)
        .expect("buffer must be a float[]");

    Ok(())
}

#[no_mangle]
pub extern "system" fn Java_net_liquidev_d3r_D3r_openOutputStream(
    env: JNIEnv,
    _: JClass,
    output_device_id: u32,
    sample_rate: u32,
    channel_count: u16,
    buffer_size: u32,
    generator: JObject,
) -> u32 {
    try_with_global_state(env, |state| {
        let Some(device) = state.devices.get(output_device_id) else { return Err("Invalid output device ID".to_string()); };
        let config = StreamConfig {
            channels: channel_count,
            sample_rate: SampleRate(sample_rate),
            buffer_size: if buffer_size == 0 {
                BufferSize::Default
            } else {
                BufferSize::Fixed(buffer_size)
            },
        };

        let class = env.get_object_class(generator).map_err(|e| e.to_string())?;
        let generate_method = env
            .get_method_id(class, "getOutputBuffer", "(II)[F")
            .map_err(|e| e.to_string())?;

        let generator_ref = env.new_global_ref(generator).map_err(|e| e.to_string())?;
        let jvm = env.get_java_vm().map_err(|e| e.to_string())?;
        let mut initialized = false;

        let stream = device
            .build_output_stream(
                &config.clone(),
                move |data: &mut [f32], _| {
                    if !initialized {
                        if let Err(error) = jvm.attach_current_thread_permanently() {
                            error!("cannot attach JVM to audio thread: {error}");
                            // TODO: propagate the error?
                            return;
                        }
                        initialized = true;
                    }
                    let env = jvm
                        .get_env()
                        .expect("thread should be attached at this point");
                    let generator = generator_ref.as_obj();
                    if let Err(error) =
                        generate_audio(data, &config, env, generator, generate_method)
                    {
                        error!("{error}");
                    }
                },
                |error| {
                    error!("{error}"); // lol
                },
            )
            .map_err(|e| e.to_string())?;
        Ok(state.streams.add(stream))
    })
}

#[no_mangle]
pub extern "system" fn Java_net_liquidev_d3r_D3r_closeOutputStream(
    env: JNIEnv,
    _: JClass,
    output_stream_id: u32,
) {
    try_with_global_state(env, |state| {
        let Some(stream) = state.devices.remove(output_stream_id) else { return Err("Invalid output stream ID"); };
        drop(stream);
        Ok(())
    });
}

#[no_mangle]
pub extern "system" fn Java_net_liquidev_d3r_D3r_startPlayback(
    env: JNIEnv,
    _: JClass,
    output_stream_id: u32,
) {
    try_with_global_state(env, |state| {
        let Some(stream) = state.streams.get(output_stream_id) else { return Err("Invalid output stream ID".to_string()); };
        stream.play().map_err(|e| e.to_string())
    });
}
