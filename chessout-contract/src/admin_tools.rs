multiversx_sc::imports!();


use crate::data_models::ContractSettings;
use crate::data_models::FeeItem;
use crate::data_models::TokenSettings;
use crate::data_models::TotalFees;
use crate::data_store;


#[multiversx_sc::module]

pub trait AdminTools: data_store::StoreModule{
    
    #[only_owner]
    #[endpoint(setContractSettings)]
    fn set_contract_settings(&self, 
        egld_processing_procentage: u64,
        xch_token: TokenIdentifier,
        xch_processing_procentage: u64
        ) {
        let egld_settings = TokenSettings {
            token_id: EgldOrEsdtTokenIdentifier::egld(),
            processing_procentage: egld_processing_procentage,
        };

        let mut settings = ContractSettings {
            token_settings: ManagedVec::new(),
        };
        settings.token_settings.push(egld_settings);


        let xch_settings = TokenSettings{
            token_id: EgldOrEsdtTokenIdentifier::esdt(xch_token),
            processing_procentage: xch_processing_procentage,
        };
        
        settings.token_settings.push(xch_settings);

        self.contract_settings().set(settings);

        // if total fees is empty initialize to empty list
        if self.total_fees().is_empty() {
            let total_fees = TotalFees {
                fee_list: ManagedVec::new(),
            };
            self.total_fees().set(total_fees);
        }
         
        

        // if fees list does not contain EgldOrEsdtTokenIdentifier::egld(), add it with 0 value
        let mut total_fees = self.total_fees().get();
        let mut egld_found = false;
        for fee_item in total_fees.fee_list.iter() {
            if fee_item.token_id == EgldOrEsdtTokenIdentifier::egld() {
                egld_found = true;
            }
        }

        if !egld_found {
            let egld_fee_item = FeeItem {
                token_id: EgldOrEsdtTokenIdentifier::egld(),
                collected_value: BigUint::zero(),
            };
            total_fees.fee_list.push(egld_fee_item);
        }

        self.total_fees().set(total_fees);
        // if fees list does not contain EgldOrEsdtTokenIdentifier::esdt(xch_token), add it with 0 value
        
        

         
    }
}