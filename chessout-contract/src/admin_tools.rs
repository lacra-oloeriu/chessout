multiversx_sc::imports!();


use crate::data_models::ContractSettings;
use crate::data_models::TokenSettings;
use crate::data_store;


#[multiversx_sc::module]

pub trait AdminTools: data_store::StoreModule{
    
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
}