import {
	AbiRegistry,
	Address, BigUIntValue, ContractFunction,
	Interaction,
	ResultsParser,
	SmartContract
} from "@multiversx/sdk-core/out";
import {refreshAccount} from "@multiversx/sdk-dapp/__commonjs/utils";
import {sendTransactions} from "@multiversx/sdk-dapp/services";
import {multiplier} from "./generalTools";
import {BigNumber} from "bignumber.js";
import {BytesValue} from "@multiversx/sdk-core/out/smartcontracts/typesystem/bytes";

// Function template to query a smart contract
export const contractQuery = async (networkProvider, abiFile, scAddress, scName, methodName, methodArgs) => {
	try {
		let abiRegistry = AbiRegistry.create(abiFile);
		let contract = new SmartContract({
			address: new Address(scAddress),
			abi: abiRegistry
		});

		let contractEndpoint = new ContractFunction(methodName);
		let interaction = new Interaction(contract, contractEndpoint, methodArgs);

		let query = interaction.check().buildQuery();
		let queryResponse = await networkProvider.queryContract(query);

		let endpointDefinition = interaction.getEndpoint();
		let resultsParser = new ResultsParser();
		let {firstValue} = resultsParser.parseQueryResponse(queryResponse, endpointDefinition);

		if (firstValue) return firstValue.valueOf();
	} catch (error) {
		console.error(error);
	}
};

// Function template to query a smart contract with multiple returned values
export const contractQueryMultipleValues = async (networkProvider, abiFile, scAddress, scName, methodName, methodArgs) => {
	try {
		let abiRegistry = AbiRegistry.create(abiFile);
		let contract = new SmartContract({
			address: new Address(scAddress),
			abi: abiRegistry
		});

		let contractEndpoint = new ContractFunction(methodName);
		let interaction = new Interaction(contract, contractEndpoint, methodArgs);

		let query = interaction.check().buildQuery();
		let queryResponse = await networkProvider.queryContract(query);

		let endpointDefinition = interaction.getEndpoint();
		let resultsParser = new ResultsParser();
		let values = resultsParser.parseQueryResponse(queryResponse, endpointDefinition);

		if (values) return values.values;
	} catch (error) {
		console.error(error);
	}
};

export const createXTournament = async (abiFile, scAddress, scToken, chainID, entryFee) => {
	try {
		let abiRegistry = AbiRegistry.create(abiFile);
		let contract = new SmartContract({
			address: new Address(scAddress),
			abi: abiRegistry
		});

		const transaction = contract.methodsExplicit
			.createTournament([
				BytesValue.fromUTF8(scToken),
				new BigUIntValue(new BigNumber(entryFee)),
			])
			.withChainID(chainID)
			.buildTransaction();
		const createTournamentTransaction = {
			value: 0,
			data: Buffer.from(transaction.getData().valueOf()),
			receiver: scAddress,
			gasLimit: '15000000'
		};
		await refreshAccount();

		const { sessionId, error } = await sendTransactions({
			transactions: createTournamentTransaction,
			transactionsDisplayInfo: {
				processingMessage: 'Processing Create Tournament transaction',
				errorMessage: 'An error has occurred during Create Tournament transaction',
				successMessage: 'Create Tournament transaction successful'
			},
			redirectAfterSign: false
		});
	} catch (error) {
		console.error(error);
	}
};