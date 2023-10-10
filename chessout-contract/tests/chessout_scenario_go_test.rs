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
fn t_002_set_setings_go() {
    world().run("scenarios/t_002_set_setings.scen.json");
}

#[test]
fn t_003_create_tournament_go() {
    world().run("scenarios/t_003_create_tournament.scen.json");
}
