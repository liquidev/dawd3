//! Registry that attaches names to objects.

use std::mem::MaybeUninit;
use bitvec::vec::BitVec;

pub struct Registry<T> {
    next_free: u32,
    free_list: Vec<u32>,
    valid: BitVec,
    store: Vec<MaybeUninit<T>>,
}

impl<T> Registry<T> {
    pub fn new() -> Self {
        Self {
            next_free: 0,
            free_list: vec![],
            valid: BitVec::new(),
            store: vec![]
        }
    }

    pub fn add(&mut self, item: T) -> u32 {
        if let Some(id) = self.free_list.pop() {
            self.valid.set(id as usize, true);
            self.store[id as usize].write(item);
            id
        } else {
            let free = self.next_free;
            self.next_free += 1;
            self.valid.push(true);
            self.store.push(MaybeUninit::new(item));
            free
        }
    }

    pub fn remove(&mut self, index: u32) -> Option<T> {
        if self.valid[index as usize] {
            self.valid.set(index as usize, false);
            let item = std::mem::replace(&mut self.store[index as usize], MaybeUninit::uninit());
            Some(unsafe { item.assume_init() })
        } else {
            None
        }
    }

    pub fn get(&self, index: u32) -> Option<&T> {
        if self.valid[index as usize] {
            Some(unsafe { self.store[index as usize].assume_init_ref() })
        } else {
            None
        }
    }

    pub fn get_mut(&mut self, index: u32) -> Option<&mut T> {
        if self.valid[index as usize] {
            Some(unsafe { self.store[index as usize].assume_init_mut() })
        } else {
            None
        }
    }
}
