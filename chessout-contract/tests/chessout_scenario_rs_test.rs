use multiversx_sc_scenario::*;

fn world() -> ScenarioWorld {
    let mut blockchain = ScenarioWorld::new();
    // blockchain.set_current_dir_from_workspace("relative path to your workspace, if applicable");

    blockchain.register_contract("file:output/chessout.wasm", chessout::ContractBuilder);
    blockchain
}

#[test]
fn t_000_chessout_rs() {
    world().run("scenarios/t_000_chessout.scen.json");
}

#[test]
fn t_001_deploy_rs() {
    world().run("scenarios/t_001_deploy.scen.json");
}

#[test]
fn t_002_set_setings_rs() {
    world().run("scenarios/t_002_set_setings.scen.json");
}

#[test]
fn t_003_create_tournament_rs() {
    world().run("scenarios/t_003_create_tournament.scen.json");
}

#[test]
fn t_004_join_tournament_rs() {
    world().run("scenarios/t_004_join_tournament.scen.json");
}

#[test]
fn t_005_set_winers_rs() {
    world().run("scenarios/t_005_set_winers.scen.json");
}

#[test]
fn t_006_distributsse_winner_funds_rs() {
    world().run("scenarios/t_006_distributsse_winner_funds.scen.json");
}
