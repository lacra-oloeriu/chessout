PROJECT="${PWD}"

CORE_LOGS="interaction/logs"
MY_DECIMALS="000000000000000000"
MY_BYTECODE="output/chessout.wasm"

setEnvDevnet() {
  CURRENT_ENV="devnet"
  ENV_LOGS="${CORE_LOGS}/${CURRENT_ENV}"

  cp -f mxpy.data-storage-devnet.json mxpy.data-storage.json
  PEM_FILE="${PROJECT}/../../wallets/devnet_owner_wallet.pem"
  ADDRESS=$(mxpy data load --key=address-devnet)
  PROXY=https://devnet-gateway.multiversx.com
  CHAINID=D

  EGLD_PROCENTAGE="20000"

  TOKEN_ID="XLH-4a7cc0"
  TOKEN_ID_HEX=$(echo -n ${TOKEN_ID} | xxd -p)
  TOKEN_PROCENTAGE="15000"

}

deployContract() {
  MY_LOGS="${ENV_LOGS}-deploy.json"
  mxpy --verbose contract deploy --bytecode ${MY_BYTECODE} --recall-nonce --pem=${PEM_FILE} \
    --gas-limit=100000000 --send --outfile="${MY_LOGS}" \
    --proxy=${PROXY} --chain=${CHAINID} || return

  TRANSACTION=$(mxpy data parse --file="${MY_LOGS}" --expression="data['emitted_tx']['hash']")
  ADDRESS=$(mxpy data parse --file="${MY_LOGS}" --expression="data['emitted_tx']['address']")

  mxpy data store --key=address-devnet --value=${ADDRESS}
  mxpy data store --key=deployTransaction-devnet --value=${TRANSACTION}

  echo ""
  echo "Smart contract address: ${ADDRESS}"
}

updateContract() {
  MY_LOGS="${ENV_LOGS}-updateContract.json"
  mxpy --verbose contract upgrade ${ADDRESS} --bytecode ${MY_BYTECODE} --recall-nonce --pem=${PEM_FILE} \
    --gas-limit=100000000 --send --outfile="${MY_LOGS}" \
    --proxy=${PROXY} --chain=${CHAINID}
}

setContractSettings(){
  MY_LOGS="${ENV_LOGS}-setContractSettings.json"
  mxpy --verbose contract call ${ADDRESS} --recall-nonce \
    --pem=${PEM_FILE} \
    --gas-limit=8000000 \
    --proxy=${PROXY} --chain=${CHAINID} \
    --function="setContractSettings" \
    --arguments ${EGLD_PROCENTAGE} "0x${TOKEN_ID_HEX}" ${TOKEN_PROCENTAGE}  \
    --send \
    --outfile="${MY_LOGS}"
}

createTournament(){
  MY_LOGS="${ENV_LOGS}-createTournament.json"
  ENTRY_FEE="1${MY_DECIMALS}"
  mxpy --verbose contract call ${ADDRESS} --recall-nonce \
    --pem=${PEM_FILE} \
    --gas-limit=8000000 \
    --proxy=${PROXY} --chain=${CHAINID} \
    --function="createTournament" \
    --arguments "0x${TOKEN_ID_HEX}" ${ENTRY_FEE}  \
    --send \
    --outfile="${MY_LOGS}"
}

joinTournament() {
  MY_LOGS="${ENV_LOGS}-joinTournament.json"
  method_name="0x$(echo -n 'joinTournament' | xxd -p -u | tr -d '\n')"
  token_id="0x$(echo -n ${TOKEN_ID} | xxd -p -u | tr -d '\n')"
  amount="1${MY_DECIMALS}"
  tournament_id="1"
  mxpy --verbose contract call ${ADDRESS} --recall-nonce \
    --pem=${PEM_FILE} \
    --gas-limit=8000000 \
    --proxy=${PROXY} --chain=${CHAINID} \
    --function="ESDTTransfer" \
    --arguments $token_id $amount $method_name $tournament_id \
    --send \
    --outfile="${MY_LOGS}"
}





