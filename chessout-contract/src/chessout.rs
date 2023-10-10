#![no_std]

multiversx_sc::imports!();
multiversx_sc::derive_imports!();

mod data_models;
mod data_store;
mod admin_tools;


#[multiversx_sc::contract]
pub trait Chessout: data_store::StoreModule 
+ admin_tools::AdminTools {
    #[init]
    fn init(&self) {
        self.last_index().set_if_empty(0)
    }


    fn get_last_index(&self) -> usize {
        if self.last_index().is_empty() {
            return 0;
        } else {
            return self.last_index().get();
        }
    }
}
