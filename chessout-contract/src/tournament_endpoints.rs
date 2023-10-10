multiversx_sc::imports!();


use crate::data_models::ContractSettings;
use crate::data_models::TokenSettings;
use crate::data_models::Tournament;
use crate::data_store;


#[multiversx_sc::module]
pub trait TournamentEndpoints: data_store::StoreModule{
    
    #[endpoint(createTournament)]
    fn create_tournament(&self, 
        token_id: TokenIdentifier,
        ) {
        
        let valid_token = self.is_token_valid(&token_id);
        require!(valid_token, "Token is not valid");

        let id :u64 = self.increment_and_get_last_index();
        let tournament_token = EgldOrEsdtTokenIdentifier::esdt(token_id);
        let tournament = Tournament{
            id: id,
            token_id: tournament_token,
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

}