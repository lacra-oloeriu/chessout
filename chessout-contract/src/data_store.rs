multiversx_sc::imports!();
multiversx_sc::derive_imports!();

use crate::data_models::ContractSettings;
use crate::data_models::Tournament;

#[multiversx_sc::module]
pub trait StoreModule {
    #[storage_mapper("contractSettings")]
    fn contract_settings(&self) -> SingleValueMapper<ContractSettings<Self::Api>>;

    #[storage_mapper("lastIndex")]
    fn last_index(&self) -> SingleValueMapper<u64>;

    #[storage_mapper("tournamentData")]
    fn tournament_data(&self, id: u64) -> SingleValueMapper<Tournament<Self::Api>>;
}
