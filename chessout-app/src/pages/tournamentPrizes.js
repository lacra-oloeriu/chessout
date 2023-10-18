import React, {useEffect, useState} from "react";
import {Link, useParams} from 'react-router-dom';
import {getTournament, getTournamentPlayers} from "utils/firebaseTools";
import {Col, Container, Row} from "react-bootstrap";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "firebase/storage";
import {getDatabase, ref as dbRef, update} from 'firebase/database';
import {Avatar, Button as MuiButton, ButtonGroup, Typography} from "@mui/material";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import chessoutAbi from 'abiFiles/chessout.abi.json';
import {customConfig, networkId} from "../config/customConfig";
import {networkConfig} from "../config/networks";
import {ProxyNetworkProvider} from "@multiversx/sdk-network-providers/out";
import {useGetAccountInfo, useGetActiveTransactionsStatus} from "@multiversx/sdk-dapp/hooks";
import {
	AbiRegistry,
	Address,
	AddressValue,
	BigUIntValue,
	BytesValue,
	SmartContract,
	U64Value
} from "@multiversx/sdk-core/out";
import {BigNumber} from "bignumber.js";
import {refreshAccount} from "@multiversx/sdk-dapp/__commonjs/utils";
import {sendTransactions} from "@multiversx/sdk-dapp/services";
import {contractQuery} from "../utils/multiversxTools";
import {useGetPendingTransactions} from "@multiversx/sdk-dapp/hooks/transactions";
import {multiplier} from "../utils/generalTools";
import Fade from "@mui/material/Fade/Fade";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField/TextField";
import Button from "@mui/material/Button";
import Modal from "@mui/material/Modal";

function TournamentPrizes(props) {
	const { tournamentId } = useParams();
	const storage = getStorage(firebaseApp);
	const [tournament, setTournament] = useState(null);
	const [players, setPlayers] = useState(null);
	const [winners, setWinners] = useState(null);
	const [defaultClubInfo, setDefaultClubInfo] = useState([]);

	//MultiversX config
	const {address} = useGetAccountInfo();
	const isLoggedIn = address.startsWith("erd");
	const config = customConfig[networkId];
	const networkProvider = new ProxyNetworkProvider(config.provider);
	const scAddress = config.scAddress;
	const scToken = config.scToken;
	const scName = "Chessout";
	const chainID = networkConfig[networkId].shortId;
	const tokensAPI = config.apiLink + address + "/tokens?size=2000";

	//Get tournament
	const getMyTournament = async () => {
		const defaultClub = await props.getMyDefaultClub();
		setDefaultClubInfo(defaultClub);
		const myTournamentData = await getTournament(defaultClub?.clubKey, tournamentId);

		// tournament players
		const tournamentPlayers = await getTournamentPlayers(defaultClub?.clubKey, tournamentId);
		const tournamentPlayersArray = tournamentPlayers ? Object.values(tournamentPlayers) : [];
		const tournamentPlayersDetails = await Promise.all(tournamentPlayersArray.map(async (player) => {
			const imageData = player.profilePictureUri ? await getDownloadURL(ref(storage, player.profilePictureUri)) : null;
			return {
				...player,
				playerImage: imageData,
			};
		}));

		//get players that have multiversXAddress from tournament players
		const eligiblePlayers = {};
		for (const playerKey in tournamentPlayers) {
			const player = tournamentPlayers[playerKey];
			if (player.multiversXAddress) {
				eligiblePlayers[playerKey] = player;
			}
		}
		const eligiblePlayersArray = eligiblePlayers ? Object.values(eligiblePlayers) : [];
		setPlayers(eligiblePlayersArray);

		const scTournamentData = myTournamentData.multiversXTournamentId ?
			await contractQuery(
				networkProvider,
				chessoutAbi,
				scAddress,
				scName,
				"getTournamentData",
				[new U64Value(myTournamentData.multiversXTournamentId)]
			) : [];

		console.log(JSON.stringify(scTournamentData, null, 2))
		// get the winning list
		const winnerList = [];
		if(scTournamentData.winner_list && scTournamentData?.winner_list.length){
			if(eligiblePlayers){
				scTournamentData.winner_list.map((item) => {
					eligiblePlayersArray.map((item2) => {
						if(item.winner.bech32() === item2.multiversXAddress){
							item2.prize = item.prize / multiplier;
							winnerList.push(item2);
						}
					})
				})
			}
		}
		setWinners(winnerList);

		//sort players based on elo
		tournamentPlayersDetails.sort((a, b) => b.elo - a.elo);
		myTournamentData.players = tournamentPlayersDetails ? tournamentPlayersDetails: null;

		setTournament(myTournamentData);
	};

	useEffect(() => {
		if (props.firebaseUser) {
			getMyTournament();
		}
	}, [props.firebaseUser]);

	// convert tournament modal
	const [open, setOpen] = useState(false);
	const openModal = () => {setOpen(true);	};
	const closeModal = () => {setOpen(false);	};

	// convert tournament function
	const [tournamentPrize, setTournamentPrize] = useState(0);
	const handleChangePrize = (e) => {
		setTournamentPrize(e.target.value);
	};

	const addTournamentWinner = async (multiversXAddress) => {
		closeModal();
		try {
			let abiRegistry = AbiRegistry.create(chessoutAbi);
			let contract = new SmartContract({
				address: new Address(scAddress),
				abi: abiRegistry
			});

			const transaction = contract.methodsExplicit
				.addTounamentWinner([
					new U64Value(tournament.multiversXTournamentId),
					new AddressValue(new Address(multiversXAddress)),
					new BigUIntValue(new BigNumber(tournamentPrize * multiplier)),
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

			const { sessionId } = await sendTransactions({
				transactions: createTournamentTransaction,
				transactionsDisplayInfo: {
					processingMessage: 'Processing Add Winner transaction',
					errorMessage: 'An error has occurred during Add Winner transaction',
					successMessage: 'Add Winner transaction successful'
				},
				redirectAfterSign: false
			});

		} catch (error) {
			console.error(error);
		}
	};

	const sendTournamentPrizes = async () => {
		closeModal();
		try {
			let abiRegistry = AbiRegistry.create(chessoutAbi);
			let contract = new SmartContract({
				address: new Address(scAddress),
				abi: abiRegistry
			});

			const transaction = contract.methodsExplicit
				.distribureTournamentRewords([
					new U64Value(tournament.multiversXTournamentId),
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

			const { sessionId } = await sendTransactions({
				transactions: createTournamentTransaction,
				transactionsDisplayInfo: {
					processingMessage: 'Processing Distribute Prizes transaction',
					errorMessage: 'An error has occurred during Distribute Prizes transaction',
					successMessage: 'Distribute Prizes transaction successful'
				},
				redirectAfterSign: false
			});

		} catch (error) {
			console.error(error);
		}
	};

	// if the transaction was made, add the multiversXTournamentId and entryFee to the firebase existing tournament
	const loadingTransactions = useGetPendingTransactions().hasPendingTransactions;
	const transactionSuccess = useGetActiveTransactionsStatus().success;

	useEffect(() => {
		if(transactionSuccess) {
			getMyTournament();
		}
	}, [transactionSuccess]);

	return(
		<Container className="mt-2 mb-5">
			<Row>
				<Col xs={12} lg={{ offset: 1, span: 10 }}>
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Row style={{ display: 'flex', alignItems: "center" }}>
							<Col xs={12} lg={3} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
								<Typography color={'#66bb6a'} variant="caption">Tournament Name</Typography>
								<Typography>{tournament?.name}</Typography>
							</Col>
							<Col xs={12} lg={3} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
								<Typography color={'#66bb6a'} variant="caption">Tournament Location</Typography>
								<Typography>{tournament?.location}</Typography>
							</Col>
							<Col xs={12} lg={3} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
								<Typography color={'#66bb6a'} variant="caption">Tournament Rounds</Typography>
								<Typography>{tournament?.totalRounds}</Typography>
							</Col>
							<Col xs={12} lg={3} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center border-end'}`}>
								<Typography color={'#66bb6a'} variant="caption">MultiversX Tournament</Typography>
								<div>
									{tournament?.multiversXTournamentId ? (
										<CheckIcon />
									) : (
										<CloseIcon />
									)}
								</div>
							</Col>
						</Row>

						<div className="text-center">
							<ButtonGroup
								variant="text"
								aria-label="navigation buttons"
								className="mt-5"
							>
								<MuiButton
									component={Link}
									to={`/tournament-players/${tournamentId}`}
									variant="text"
									className="me-2"
									style={{
										borderRadius: 0,
										border: 'none',
										backgroundColor: 'transparent',
										color: 'white'
									}}
								>
									Players
								</MuiButton>
								<MuiButton
									component={Link}
									to={`/tournament-rounds/${tournamentId}/0`}
									variant="text"
									className="me-2"
									style={{
										borderRadius: 0,
										border: 'none',
										backgroundColor: 'transparent',
										color: 'white'
									}}
								>
									Rounds
								</MuiButton>
								<MuiButton
									component={Link}
									to={`/tournament-standings/${tournamentId}`}
									variant="text"
									style={{
										borderRadius: 0,
										border: 'none',
										backgroundColor: 'transparent',
										color: 'white'
									}}
								>
									Standings
								</MuiButton>
								<MuiButton
									component={Link}
									to={`/tournament-join-requests/${tournamentId}`}
									variant="text"
									className="ms-2"
									style={{
										borderRadius: 0,
										border: 'none',
										backgroundColor: 'transparent',
										color: 'white'
									}}
								>
									Join Requests
								</MuiButton>
								<MuiButton
									variant="text"
									color="success"
									className="ms-2"
									style={{
										borderBottom: '1px solid #66bb6a',
										borderRadius: 0,
										backgroundColor: 'transparent',
									}}
								>
									Prizes
								</MuiButton>
							</ButtonGroup>
						</div>
					</div>

					<Row>
						<Col xs={12} lg={{offset: 6, span: 3}} className="mt-3">
							<button className="btn btn-outline-success b-r-xs btn-block btn-sm" onClick={() => sendTournamentPrizes()}>
								Send Tournament Prizes
							</button>
						</Col>
						<Col xs={12} lg={3} className="mt-3">
							<button className="btn btn-outline-success b-r-xs btn-block btn-sm" onClick={openModal}>
								Add Tournament Winner
							</button>
						</Col>
					</Row>

					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Typography variant="h6" className="text-center">Tournament Winners</Typography>
						{winners && winners.length > 0 ? (
							<TableContainer>
								<Table sx={{ minWidth: 650 }} aria-label="simple table">
									<TableHead>
										<TableRow>
											<TableCell align={"center"} width={"10%"} style={{color: '#66bb6a'}}>No</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Avatar</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Name</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Elo</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Prize</TableCell>
										</TableRow>
									</TableHead>
									<TableBody>
										{winners.map((player, index) => (
											<TableRow
												key={index}
												sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
											>
												<TableCell component="th" scope="row" align={"center"}>
													{index + 1}.
												</TableCell>
												<TableCell>
													{player.playerImage ? (
														<Avatar aria-label="player" src={player.playerImage} sx={{ width: 55, height: 55}}/>
													):(
														<Avatar aria-label="player" sx={{ width: 55, height: 55, backgroundColor: 'transparent'}}>
															<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white'}}/>
														</Avatar>
													)}
												</TableCell>
												<TableCell component="th" scope="row">
													{player.name}
												</TableCell>
												<TableCell>{player.elo}</TableCell>
												<TableCell>{player.prize}</TableCell>
											</TableRow>
										))}
									</TableBody>
								</Table>
							</TableContainer>
						) : (
							<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px'}}><p>No winners</p></div>
						)}
					</div>
				</Col>
			</Row>

			{/* convert tournament*/}
			<Modal open={open} onClose={closeModal} >
				<Fade in={open}>
					<Box
						sx={{
							position: 'absolute',
							width: '50%',
							backgroundColor: '#121212',
							top: '50%',
							left: '50%',
							transform: 'translate(-50%, -50%)',
							padding: '40px',
							borderRadius: 2,
							opacity: 1,
							textAlign: 'center'
						}}
					>
						<h3 className="mb-3">Add winner</h3>
						<div className="mt-3" style={{ maxHeight: '300px', overflowY: 'auto' }}>
							{players ? (
								<React.Fragment>
									<div className="mt-2 mb-3">
										<TextField
											name="prize"
											label="Prize"
											type="number"
											value={tournamentPrize}
											onChange={handleChangePrize}
											size="small"
											style={{width: '40%'}}
										/>
									</div>
									{players.map((player, index) => (
										<div className="d-flex justify-content-between align-items-center border-bottom py-2 px-2" key={index}>
											<div className="d-flex align-items-center">
												{player.playerImage ? (
													<Avatar aria-label="player" src={player.playerImage} sx={{ width: 45, height: 45 }} />
												) : (
													<Avatar aria-label="player" sx={{ width: 45, height: 45, backgroundColor: 'transparent' }}>
														<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white' }} />
													</Avatar>
												)}
												<p className="m-0 ms-3">{player.name} ({player.elo})</p>
											</div>
											<button className="btn btn-sm btn-outline-success b-r-xs" onClick={() => addTournamentWinner(player.multiversXAddress)}>Add Winner</button>
										</div>
									))}
								</React.Fragment>
							) : (
								<div className="text-center align-content-center b-r-sm mt-5" style={{ backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px' }}>
									<p>No eligible players.</p>
								</div>
							)}
						</div>
					</Box>
				</Fade>
			</Modal>
		</Container>
	);
}

export default TournamentPrizes;