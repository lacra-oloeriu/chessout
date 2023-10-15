multiversx_sc::imports!();

use crate::data_models::ContractSettings;
use crate::data_models::TokenSettings;
use crate::data_models::Tournament;
use crate::data_models::TournamentWiner;
use crate::data_store;
use core::ops::Deref;

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
        let winner_list = ManagedVec::new();

        let zero_initial_fund: BigUint = BigUint::zero();
        let tournament = Tournament {
            id: id,
            token_id: tournament_token,
            entry_fee: entry_fee,
            available_funds: zero_initial_fund,
            manager_list: manager_list,
            participant_list: participant_list,
            winner_list: winner_list,
            prizes_have_bean_distibuted: false,
        };

        self.tournament_data(id).set(tournament);

        //set my last created id
        let caller = self.blockchain().get_caller();
        sc_print!("id = {}", id);
        self.my_last_created_id(&caller).set(id);
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

    fn is_tournament_token_valid(
        &self,
        tournament_id: u64,
        token: EgldOrEsdtTokenIdentifier,
    ) -> bool {
        let tournament = self.tournament_data(tournament_id).get();
        if tournament.token_id == token {
            return true;
        }
        return false;
    }

    fn is_tournament_entry_fee_valid(&self, tournament_id: u64, entry_fee: &BigUint) -> bool {
        let tournament = self.tournament_data(tournament_id).get();
        if tournament.entry_fee == *entry_fee {
            return true;
        }
        return false;
    }

    // check is participant part of tournament
    fn participant_can_join_tournament(
        &self,
        tournament_id: u64,
        participant: &ManagedAddress,
    ) -> bool {
        let tournament = self.tournament_data(tournament_id).get();
        for participant_address in tournament.participant_list.iter() {
            if participant_address.deref() == participant {
                return false;
            }
        }
        true
    }

    fn participant_is_part_of_tournament(
        &self,
        tournament_id: u64,
        participant: &ManagedAddress,
    ) -> bool {
        let tournament = self.tournament_data(tournament_id).get();
        for participant_address in tournament.participant_list.iter() {
            if participant_address.deref() == participant {
                return true;
            }
        }
        false
    }

    // add participant to tournament
    fn add_participant_to_tournament(
        &self,
        tournament_id: u64,
        participant: &ManagedAddress,
        payment: &BigUint,
    ) {
        let mut tournament = self.tournament_data(tournament_id).get();
        tournament.participant_list.push(participant.clone()); // You might need to clone the participant if it's not Copy.
        tournament.available_funds += payment.clone(); // You might need to clone the payment if it's not Copy.
        self.tournament_data(tournament_id).set(tournament);
    }

    fn add_winner_to_tournament(
        &self,
        tournament_id: u64,
        winner: &ManagedAddress,
        prize: &BigUint,
    ) {
        let mut tournament = self.tournament_data(tournament_id).get();
        tournament.winner_list.push(TournamentWiner {
            winner: winner.clone(),
            prize: prize.clone(),
        }); // You might need to clone the participant if it's not Copy.
        self.tournament_data(tournament_id).set(tournament);
    }

    #[payable("*")]
    #[endpoint(joinTournament)]
    fn join_tournament(&self, tournament_id: u64) {
        // check token
        let payment = self.call_value().egld_or_single_esdt();
        let join_token = payment.token_identifier;
        let valid_token = self.is_tournament_token_valid(tournament_id, join_token);
        require!(valid_token, "Tournament token is not valid");

        // check entry fee
        let entry_fee = payment.amount;
        let valid_entry_fee = self.is_tournament_entry_fee_valid(tournament_id, &entry_fee);
        require!(valid_entry_fee, "Tournament entry fee is not valid");

        let participant = self.blockchain().get_caller();
        let can_join = self.participant_can_join_tournament(tournament_id, &participant);
        require!(can_join, "Participant is already part of tournament");

        // add participant to tournament
        self.add_participant_to_tournament(tournament_id, &participant, &entry_fee);
    }

    fn is_tourament_manager(&self, tournament_id: u64, manager: &ManagedAddress) -> bool {
        let tournament = self.tournament_data(tournament_id).get();
        for manager_address in tournament.manager_list.iter() {
            if manager_address.deref() == manager {
                return true;
            }
        }
        false
    }

    #[endpoint(addTounamentWinner)]
    fn addTounamentWinner(&self, tournament_id: u64, winner: ManagedAddress, prize: BigUint) {
        // check if caller is manager
        let manager = self.blockchain().get_caller();
        let is_manager = self.is_tourament_manager(tournament_id, &manager);
        require!(is_manager, "Caller is not manager of tournament");

        let is_part_of_tournament = self.participant_is_part_of_tournament(tournament_id, &winner);
        require!(is_part_of_tournament, "Winner is not part of tournament");

        self.add_winner_to_tournament(tournament_id, &winner, &prize);
    }

    fn calculate_fees_for_total(
        &self,
        token_id: EgldOrEsdtTokenIdentifier,
        total: BigUint,
    ) -> BigUint {
        let settings = self.contract_settings().get();
        for token_setting in settings.token_settings.iter() {
            if token_setting.token_id == token_id {
                let fee = (total * token_setting.processing_procentage) / (100u64 * 10000u64);
                return fee;
            }
        }
        sc_panic!("No donfiguration for token {}", token_id);
    }

    #[endpoint(distribureTournamentRewords)]
    fn distribute_tournament_rewords(&self, tournament_id: u64) {
        // check if caller is manager
        let manager = self.blockchain().get_caller();
        let is_manager = self.is_tourament_manager(tournament_id, &manager);
        require!(is_manager, "Caller is not manager of tournament");

        let tournament = self.tournament_data(tournament_id).get();
        let mut total_rewords = BigUint::zero();
        for t_winner in tournament.winner_list.iter() {
            total_rewords += &t_winner.prize;
            self.send()
                .direct(&t_winner.winner, &tournament.token_id, 0, &t_winner.prize);
        }

        sc_print!("tota rewords = {}", total_rewords);
        let t_funds =  self.tournament_data(tournament_id).get().available_funds.clone();
        let tournament_fee = self.calculate_fees_for_total(tournament.token_id, total_rewords.clone());

        if ((total_rewords.clone() + tournament_fee.clone()) > t_funds.clone()) {
            sc_panic!("Not enough funds to distribute");
        }
        
        // locate total fees by tournament token
        let t_token =  self.tournament_data(tournament_id).get().token_id.clone();
        let mut total_fees = self.total_fees().get();
        let mut id = 0_usize;
        for i in 1..total_fees.fee_list.len() {
            let mut fee_item = total_fees.fee_list.get(i);
            if fee_item.token_id == t_token {
                id = i;
                fee_item.collected_value += tournament_fee.clone();
                sc_print!("fee_item.collected_value = {}", fee_item.collected_value);
                break;
            }
        }
        self.total_fees().set(total_fees);

        // remove total_rewords + tournament_fee from tournament funds
        let mut tournament = self.tournament_data(tournament_id).get();
        tournament.available_funds -= (total_rewords + tournament_fee);
        sc_print!("tournament.available_funds = {}", tournament.available_funds);
        self.tournament_data(tournament_id).set(tournament);



    }
}
