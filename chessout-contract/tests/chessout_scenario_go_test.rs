use multiversx_sc_scenario::*;

fn world() -> ScenarioWorld {
    ScenarioWorld::vm_go()
}

#[test]
fn t_000_chessout_go() {
    world().run("scenarios/t_000_chessout.scen.json");
}

#[test]
fn t_001_deploy_go() {
    world().run("scenarios/t_001_deploy.scen.json");
}

#[test]
fn t_002_increment_last_index_go() {
    world().run("scenarios/t_002_increment_last_index.scen.json");
}

#[test]
fn t_003_set_setings_go() {
    world().run("scenarios/t_003_set_setings.scen.json");
}
