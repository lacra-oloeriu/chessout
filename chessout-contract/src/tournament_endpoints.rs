multiversx_sc::imports!();

use crate::data_models::ContractSettings;
use crate::data_models::TokenSettings;
use crate::data_models::Tournament;
use crate::data_store;

#[multiversx_sc::module]
pub trait TournamentEndpoints: data_store::StoreModule {
    #[endpoint(createTournament)]
    fn create_tournament(&self, token_id: TokenIdentifier, entry_fee: BigUint) {
        let valid_token = self.is_token_valid(&token_id);
        require!(valid_token, "Token is not valid");

        let id: u64 = self.increment_and_get_last_index();
        let tournament_token = EgldOrEsdtTokenIdentifier::esdt(token_id);

        let manager = self.blockchain().get_caller();
        let mut manager_list = ManagedVec::new();
        manager_list.push(manager);

        let participant_list = ManagedVec::new();

        let zero_initial_fund: BigUint = BigUint::zero();
        let tournament = Tournament {
            id: id,
            token_id: tournament_token,
            entry_fee: entry_fee,
            available_funds: zero_initial_fund,
            manager_list: manager_list,
            participant_list: participant_list,
        };

        self.tournament_data(id).set(tournament);
    }

    fn is_token_valid(&self, token_id: &TokenIdentifier) -> bool {
        let settings = self.contract_settings().get();
        for token_setting in settings.token_settings.iter() {
            if token_setting.token_id == *token_id {
                return true;
            }
        }
        return false;
    }

    fn increment_and_get_last_index(&self) -> u64 {
        let mut last_index = self.last_index().get();
        last_index += 1;
        self.last_index().set(last_index);
        return last_index;
    }

    fn is_tournament_token_valid(&self, tournament_id: u64, token: EgldOrEsdtTokenIdentifier) -> bool {
        let tournament = self.tournament_data(tournament_id).get();
        if tournament.token_id == token {
            return true;
        }
        return false;
    }

    #[payable("*")]
    #[endpoint(joinTournament)]
    fn join_tournament(&self, tournament_id: u64) {
        let payment = self.call_value().egld_or_single_esdt();
        let join_token = payment.token_identifier;
        let valid_token = self.is_tournament_token_valid(tournament_id, join_token);
        require!(valid_token, "Tournament token is not valid");
    }
}
