multiversx_sc::imports!();
multiversx_sc::derive_imports!();

use crate::data_models::ContractSettings;

#[multiversx_sc::module]
pub trait StoreModule {
    #[storage_mapper("contractSettings")]
    fn contract_settings(&self) -> SingleValueMapper<ContractSettings<Self::Api>>;

    #[storage_mapper("lastIndex")]
    fn last_index(&self) -> SingleValueMapper<usize>;
}
