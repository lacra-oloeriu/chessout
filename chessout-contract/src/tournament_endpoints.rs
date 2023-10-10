multiversx_sc::imports!();


use crate::data_models::ContractSettings;
use crate::data_models::TokenSettings;
use crate::data_store;


#[multiversx_sc::module]
pub trait TournamentEndpoints: data_store::StoreModule{
    
    #[endpoint(createTournament)]
    fn create_tournament(&self, 
        token_id: TokenIdentifier,
        ) {
        
        let valid_token = self.is_token_valid(token_id);
        require!(valid_token, "Token is not valid");
        
    }

    fn is_token_valid(&self, token_id: TokenIdentifier) -> bool {
        let settings = self.contract_settings().get();
        for token_setting in settings.token_settings.iter() {
            if token_setting.token_id == token_id {
                return true;
            }
        }
        return false;
    }
}