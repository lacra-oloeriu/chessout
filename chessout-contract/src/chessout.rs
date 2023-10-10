#![no_std]

multiversx_sc::imports!();
multiversx_sc::derive_imports!();

mod admin_tools;
mod data_models;
mod data_store;
mod tournament_endpoints;

#[multiversx_sc::contract]
pub trait Chessout:
    data_store::StoreModule 
    + admin_tools::AdminTools 
    + tournament_endpoints::TournamentEndpoints
{
    #[init]
    fn init(&self) {
        self.last_index().set_if_empty(0)
    }

    
}
