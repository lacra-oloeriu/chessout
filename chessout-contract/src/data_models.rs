multiversx_sc::imports!();
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

#[derive(TypeAbi, TopEncode, TopDecode, ManagedVecItem, NestedEncode, NestedDecode, Clone)]
pub struct Tournament<M: ManagedTypeApi> {
    pub id: u64,
    pub token_id: EgldOrEsdtTokenIdentifier<M>,
    pub entry_fee: BigUint<M>,
    pub available_funds: BigUint<M>,
    pub manager_list: ManagedVec<M, ManagedAddress<M>>,
    pub participant_list: ManagedVec<M, ManagedAddress<M>>,
}

#[derive(TypeAbi, TopEncode, TopDecode, ManagedVecItem, NestedEncode, NestedDecode, Clone)]
pub struct TotalFees<M: ManagedTypeApi> {
    pub fee_list: ManagedVec<M, FeeItem<M>>,
}

#[derive(TypeAbi, TopEncode, TopDecode, ManagedVecItem, NestedEncode, NestedDecode, Clone)]
pub struct FeeItem<M: ManagedTypeApi> {
    pub token_id: EgldOrEsdtTokenIdentifier<M>,
    pub collected_value: BigUint<M>,
}
