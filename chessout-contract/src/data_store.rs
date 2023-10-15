multiversx_sc::imports!();
multiversx_sc::derive_imports!();

use crate::data_models::ContractSettings;
use crate::data_models::TotalFees;
use crate::data_models::Tournament;

#[multiversx_sc::module]
pub trait StoreModule {
    #[view(getContractSettings)]
    #[storage_mapper("contractSettings")]
    fn contract_settings(&self) -> SingleValueMapper<ContractSettings<Self::Api>>;

    #[view(getTotalFees)]
    #[storage_mapper("totalFees")]
    fn total_fees(&self) -> SingleValueMapper<TotalFees<Self::Api>>;

    #[view(getLastIndex)]
    #[storage_mapper("lastIndex")]
    fn last_index(&self) -> SingleValueMapper<u64>;

    #[view(getTournamentData)]
    #[storage_mapper("tournamentData")]
    fn tournament_data(&self, id: u64) -> SingleValueMapper<Tournament<Self::Api>>;

    #[view(getMyLastCreatedId)]
    #[storage_mapper("myLastCreatedId")]
    fn my_last_created_id(
        &self,
        client_address: &ManagedAddress,
    ) -> SingleValueMapper<u64>;

}
