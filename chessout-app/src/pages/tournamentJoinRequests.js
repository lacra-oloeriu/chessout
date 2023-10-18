import React, {useEffect, useState} from "react";
import {Link, useParams} from 'react-router-dom';
import {getTournament, getTournamentPlayers, getClubPlayers, getTournamentPlayersRequests} from "utils/firebaseTools";
import {Col, Container, Row} from "react-bootstrap";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "firebase/storage";
import {getDatabase, ref as dbRef, update, remove} from 'firebase/database';
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
	SmartContract, TokenTransfer,
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

function TournamentJoinRequests(props) {
	const { tournamentId } = useParams();
	const storage = getStorage(firebaseApp);
	const [tournament, setTournament] = useState(null);
	const [availablePlayers, setAvailablePlayers] = useState(null);
	const [playersRequests, setPlayersRequests] = useState(null);
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
		const clubPlayers = await getClubPlayers(defaultClub?.clubKey);
		const tournamentPlayers = await getTournamentPlayers(defaultClub?.clubKey, tournamentId);
		const tournamentPlayersArray = tournamentPlayers ? Object.values(tournamentPlayers) : [];

		// get the tournament players requests
		const tournamentPlayersRequests = await getTournamentPlayersRequests(defaultClub?.clubKey, tournamentId);
		const tournamentPlayersRequestsArray = tournamentPlayersRequests ? Object.values(tournamentPlayersRequests) : [];

		const requestPlayersDetails = await Promise.all(tournamentPlayersRequestsArray.map(async (player) => {
			const imageData = player.profilePictureUri ? await getDownloadURL(ref(storage, player.profilePictureUri)) : null;
			return {
				...player,
				playerImage: imageData,
			};
		}));
		requestPlayersDetails.sort((a, b) => b.elo - a.elo);
		setPlayersRequests(requestPlayersDetails);

		myTournamentData.players = tournamentPlayersArray ? tournamentPlayersArray: null;
		setTournament(myTournamentData);

		// get the available players to join
		const playersNotInTournament = {};
		if (tournamentPlayers && Object.keys(tournamentPlayers).length > 0) {
			for (const playerKey in clubPlayers) {
				if (!tournamentPlayers[playerKey]) {
					if (!tournamentPlayersRequests || !tournamentPlayersRequests[playerKey]) {
						playersNotInTournament[playerKey] = clubPlayers[playerKey];
					}
				}
			}
		} else {
			for (const playerKey in clubPlayers) {
				if (!tournamentPlayersRequests || !tournamentPlayersRequests[playerKey]) {
					playersNotInTournament[playerKey] = clubPlayers[playerKey];
				}
			}
		}
		const playersNotInTournamentArray = playersNotInTournament ? Object.values(playersNotInTournament) : [];
		playersNotInTournamentArray.sort((a, b) => b.elo - a.elo);
		setAvailablePlayers(playersNotInTournamentArray);

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

	const joinXTournament = async (player) => {
		const serializedObject = JSON.stringify(player);
		localStorage.setItem('localPlayer', serializedObject);
		try {
			let abiRegistry = AbiRegistry.create(chessoutAbi);
			let contract = new SmartContract({
				address: new Address(scAddress),
				abi: abiRegistry
			});

			const transaction = contract.methodsExplicit
				.joinTournament([new U64Value(tournament.multiversXTournamentId)])
				.withChainID(chainID)
				.withSingleESDTTransfer(
					TokenTransfer.fungibleFromAmount(scToken, tournament.entryFee, 18)
				)
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
					processingMessage: 'Processing Join Tournament transaction',
					errorMessage: 'An error has occurred during Join Tournament transaction',
					successMessage: 'Join Tournament transaction successful'
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
			const joinTheTournament = async () => {
				const serializedObject = localStorage.getItem('localPlayer');
				const localPlayer = JSON.parse(serializedObject);
				if(localPlayer) localPlayer.multiversXAddress = address;


				const database = getDatabase(firebaseApp);
				const updateData = {
					[`tournamentPlayersRequests/${defaultClubInfo.clubKey}/${tournament?.tournamentId}/${localPlayer.playerKey}`]: localPlayer,
				};

				update(dbRef(database), updateData)
					.then(() => {
						console.log("Added Player request.");
					})
					.catch((error) => {
						console.error("Error Adding Player request:", error);
					});
			};
			joinTheTournament();
			getMyTournament();
		}
	}, [transactionSuccess]);

	const deletePlayerRequest = async (playerKey) => {
		const database = getDatabase(firebaseApp);
		const playerRequestRef = dbRef(database, `tournamentPlayersRequests/${defaultClubInfo.clubKey}/${tournament.tournamentId}/${playerKey}`);

		try {
			await remove(playerRequestRef);
			console.log("Player request deleted successfully.");
		} catch (error) {
			console.error("Error deleting Player request:", error);
		}
	};


	const acceptPlayer = async (playerRequest) => {
		const database = getDatabase(firebaseApp);
		const updateData = {
			[`tournamentPlayers/${defaultClubInfo.clubKey}/${tournament.tournamentId}/${playerRequest.playerKey}`]: playerRequest,
		};

		update(dbRef(database), updateData)
			.then(() => {
				console.log("Accepted player: " + playerRequest.playerKey);
			})
			.catch((error) => {
				console.error("Error Adding Player request:", error);
			});

		await deletePlayerRequest(playerRequest.playerKey);
		await getMyTournament();
	};

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
									className="me-2"
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
									variant="text"
									color={'success'}
									style={{
										borderRadius: 0,
										borderBottom: ' 1px solid #66bb6a',
										backgroundColor: 'transparent',
										borderRight: 0
									}}
								>
									Join Requests
								</MuiButton>
								<MuiButton
									component={Link}
									to={`/tournament-prizes/${tournamentId}`}
									variant="text"
									className="ms-2"
									style={{
										borderRadius: 0,
										border: 'none',
										backgroundColor: 'transparent',
										color: 'white'
									}}
								>
									Prizes
								</MuiButton>
							</ButtonGroup>
						</div>
					</div>

					<Row>
						<Col xs={12} lg={{offset: 9, span: 3}} className="mt-3">
							<button className="btn btn-outline-success b-r-xs btn-block btn-sm" onClick={openModal}>
								Join Player
							</button>
						</Col>
					</Row>
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Typography variant="h6" className="text-center">Tournament Join Requests</Typography>
						{playersRequests && playersRequests.length > 0 ? (
							<TableContainer>
								<Table sx={{ minWidth: 650 }} aria-label="simple table">
									<TableHead>
										<TableRow>
											<TableCell align={"center"} width={"10%"} style={{color: '#66bb6a'}}>No</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Avatar</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Name</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Elo</TableCell>
											<TableCell style={{color: '#66bb6a'}}>Actions</TableCell>
										</TableRow>
									</TableHead>
									<TableBody>
										{playersRequests.map((player, index) => (
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
												<TableCell><button className="btn btn-sm btn-success b-r-xs" onClick={() => acceptPlayer(player)}>Add Player</button></TableCell>
											</TableRow>
										))}
									</TableBody>
								</Table>
							</TableContainer>
						) : (
							<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px'}}><p>No players.</p></div>
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
							width: '40%',
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
						<h3 className="mb-1">Select player to add</h3>
						<p className="mb-3 text-light-success">Entry fee: {tournament?.entryFee}</p>
						<div className="mt-3" style={{ maxHeight: '300px', overflowY: 'auto' }}>
							{availablePlayers ? (
								<React.Fragment>
									{availablePlayers.map((player, index) => (
										<div className="d-flex justify-content-between align-items-center border-bottom py-2 px-2" key={index}>
											<p className="m-0">{index + 1}. {player.name} ({player.elo})</p>
											<button className="btn btn-sm btn-outline-success b-r-xs" onClick={() => joinXTournament(player)}>Add Player</button>
										</div>
									))}
								</React.Fragment>
							) : (
								<div className="text-center align-content-center b-r-sm mt-5" style={{ backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px' }}>
									<p>No players.</p>
								</div>
							)}
						</div>
					</Box>
				</Fade>
			</Modal>
		</Container>
	);
}

export default TournamentJoinRequests;