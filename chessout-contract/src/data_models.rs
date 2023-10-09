use multiversx_sc::types::{ManagedAddress, ManagedBuffer};
use multiversx_sc::{
    api::ManagedTypeApi,
    types::{BigUint, EgldOrEsdtTokenIdentifier, ManagedVec},
};

multiversx_sc::derive_imports!();

#[derive(TypeAbi, TopEncode, TopDecode, ManagedVecItem, NestedEncode, NestedDecode)]
pub struct ContractSettings<M: ManagedTypeApi> {
    pub token_settings: ManagedVec<M, TokenSettings<M>>,
}

#[derive(TypeAbi, TopEncode, TopDecode, ManagedVecItem, NestedEncode, NestedDecode, Clone)]
//// in 4 decimal (20000 = 2%) (10000 = 1%) (5000 = 0.5%)
pub struct TokenSettings<M: ManagedTypeApi> {
    pub token_id: EgldOrEsdtTokenIdentifier<M>,
    pub processing_procentage: u64,
}
