#![no_std]

multiversx_sc::imports!();
multiversx_sc::derive_imports!();

mod data_models;
use data_models::*;
mod data_store;


#[multiversx_sc::contract]
pub trait Chessout: data_store::StoreModule {
    #[init]
    fn init(&self) {
        self.last_index().set_if_empty(0)
    }

    #[only_owner]
    #[endpoint(setContractSettings)]
    fn set_contract_settings(&self, egld_processing_procentage: u64) {
        let egld_settings = TokenSettings {
            token_id: EgldOrEsdtTokenIdentifier::egld(),
            processing_procentage: egld_processing_procentage,
        };

        let mut settings = ContractSettings {
            token_settings: ManagedVec::new(),
        };
        settings.token_settings.push(egld_settings);
        self.contract_settings().set(settings);
    }

    #[only_owner]
    #[endpoint(incrementLastIndex)]
    fn increment_last_index(&self) {
        let mut id = self.last_index().get();
        id += 1;
        self.last_index().set(id)
    }

    fn get_last_index(&self) -> usize {
        if self.last_index().is_empty() {
            return 0;
        } else {
            return self.last_index().get();
        }
    }
}
