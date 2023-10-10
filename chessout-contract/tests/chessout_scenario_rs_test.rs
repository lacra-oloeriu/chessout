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
fn t_002_increment_last_index_rs() {
    world().run("scenarios/t_002_increment_last_index.scen.json");
}

#[test]
fn t_003_set_setings_rs() {
    world().run("scenarios/t_003_set_setings.scen.json");
}
