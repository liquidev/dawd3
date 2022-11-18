plugins {
    id("fr.stardustenterprises.rust.wrapper")
}

rust {
    command.set("cargo")
    cargoInstallTargets.set(true)

    release.set(true)

//    targets += target("x86_64-pc-windows-gnu", "dawd3_d3r.dll")
    targets += target("x86_64-unknown-linux-gnu", "libdawd3_d3r.so")
}
