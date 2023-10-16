import React, {useEffect, useState} from "react";
import { Link, useParams } from 'react-router-dom';
import {getTournament, getClub, getClubProfilePicture, getTournamentPlayers, getUserProfilePicture, getTournamentRoundGamesDecoded, getTournamentRoundGames} from "utils/firebaseTools";
import {Col, Container, Row} from "react-bootstrap";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "@firebase/storage";
import { Avatar, Typography, CardHeader, CardContent, CardMedia, Card, CardActions, IconButton, Tab, Tabs } from "@mui/material";
import {Button} from "react-bootstrap";
import Divider from "@mui/material/Divider";
import Tooltip from "@mui/material/Tooltip";
import Fade from "@mui/material/Fade";
import DoubleArrowIcon from "@mui/material/SvgIcon/SvgIcon";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import Carousel from 'react-bootstrap/Carousel';

const componentsProps={
	tooltip: {
		sx: {
			maxWidth: '200px',
			backgroundColor: 'black',
			color: 'white',
			fontSize: '14px',
			fontWeight: '400',
			textAlign: 'center',
			borderRadius: '10px',
			padding: '10px',
			top: '-10px'
		},
	},
	arrow: {
		sx: {
			color: 'black',
		},
	},
	TransitionComponent: Fade,
};

function Tournament(props) {
	const { tournamentId } = useParams();
	const storage = getStorage(firebaseApp);
	const currentTimestamp = Date.now();
	const [tournament, setTournament] = useState(null);
	const [rounds, setRounds] = useState(null);

	function roundResultWinnerColor(result){
		switch (result) {
			case 0: return {whitePlayer: '', blackPlayer: ''};
			case 1: return {whitePlayer: 'text-pink-purple', blackPlayer: ''};
			case 2: return {whitePlayer: '', blackPlayer: 'text-pink-purple'};
			case 3: return {whitePlayer: 'text-pink-purple', blackPlayer: 'text-pink-purple'};
			case 5: return {whitePlayer: 'text-pink-purple', blackPlayer: ''};
			case 6: return {whitePlayer: '', blackPlayer: 'text-pink-purple'};
			case 7: return {whitePlayer: '', blackPlayer: ''};
			case 8: return {whitePlayer: '', blackPlayer: ''};
		}
	}

	function roundResultLabels(result){
		switch (result) {
			case 0: return (<Typography variant="body1" fontSize="20px"><span className="font-bold">---</span></Typography>);
			case 1: return (<Typography variant="body1" fontSize="17px"><span className="text-pink-purple font-bold">1</span> - 0</Typography>);
			case 2: return (<Typography variant="body1" fontSize="17px">0 - <span className="text-pink-purple font-bold">1</span></Typography>);
			case 3: return (<Typography variant="body1" fontSize="17px"><span className="text-pink-purple font-bold">1/2 - 1/2</span></Typography>);
			case 5: return (<Typography variant="body1" fontSize="17px"><span className="text-pink-purple font-bold">1ff</span> - 0</Typography>);
			case 6: return (<Typography variant="body1" fontSize="17px">0 - <span className="text-pink-purple font-bold">1ff</span></Typography>);
			case 7: return (<Typography variant="body1" fontSize="17px">0ff - 0ff</Typography>);
			case 8: return (<Typography variant="body1" fontSize="17px">1/2ff - 1/2ff</Typography>);
		}
	}

	function roundResultTooltips(result, whitePlayerName, blackPlayerName){
		switch (result) {
			case 0: return '';
			case 1: return whitePlayerName + ' wins';
			case 2: return blackPlayerName + ' wins';
			case 3: return 'Draw';
			case 5: return whitePlayerName + ' wins by forfeit';
			case 6: return blackPlayerName + ' wins by forfeit';
			case 7: return 'Double forfeit';
			case 8: return 'Referee decision';
		}
	}

	//Get tournament
	const getMyTournament = async () => {
		const defaultClub = await props.getMyDefaultClub();
		const myTournamentData = await getTournament(defaultClub?.clubKey, tournamentId);

		// tournament players
		const tournamentPlayers = await getTournamentPlayers(defaultClub?.clubKey, tournamentId);
		const tournamentPlayersArray = tournamentPlayers ? Object.values(tournamentPlayers) : [];
		const tournamentPlayersCount = tournamentPlayers ? Object.keys(tournamentPlayers).length : 0;
		const tournamentPlayersDetails = await Promise.all(tournamentPlayersArray.map(async (player) => {
			const imageData = player.profilePictureUri ? await getDownloadURL(ref(storage, player.profilePictureUri)) : null;
			return {
				...player,
				playerImage: imageData,
			};
		}));

		//sort players based on elo
		tournamentPlayersDetails.sort((a, b) => b.elo - a.elo);
		myTournamentData.players = tournamentPlayersDetails;
		myTournamentData.playersCount = tournamentPlayersCount;

		//get the number of rounds and the completed / total games for each one
		const roundsInfo = [];
		for (let i = 1; i <= myTournamentData?.totalRounds; i++) {
			const tournamentRoundGamesDecodedData = await getTournamentRoundGamesDecoded(defaultClub?.clubKey, tournamentId, i);
			if (tournamentRoundGamesDecodedData) {
				const tournamentRoundGames = await getTournamentRoundGames(defaultClub?.clubKey, tournamentId, i);

				const roundsGamesResults = [];
				for (const result of tournamentRoundGames) {
					if (result) {
						roundsGamesResults.push({
							result: result.result,
							whitePlayerName: result.whitePlayer.name,
							whitePlayerImage: result.whitePlayer?.profilePictureUri ? await getDownloadURL(ref(storage, result.whitePlayer.profilePictureUri)) : null,
							blackPlayerName: result.blackPlayer.name,
							blackPlayerImage: result.blackPlayer?.profilePictureUri ? await getDownloadURL(ref(storage, result.blackPlayer.profilePictureUri)) : null,
						})
					}
				}

				roundsInfo.push({
					round: i,
					totalGames: tournamentRoundGamesDecodedData.totalGames,
					completedGames: tournamentRoundGamesDecodedData.completedGames,
					results: roundsGamesResults
				})
			}
		}
		//console.log(JSON.stringify(roundsInfo, null, 2));

		setTournament(myTournamentData);
		setRounds(roundsInfo);
	};

	useEffect(() => {
		if (props.firebaseUser) {
			getMyTournament();
		}
	}, [props.firebaseUser]);

	const [activeTab, setActiveTab] = useState(1);
	const handleTabChange = (event, newValue) => {
		setActiveTab(newValue);
	};

	//
	const [index, setIndex] = useState(0);
	const handleSelect = (selectedIndex) => {
		setIndex(selectedIndex);
	};

	return(
		<Container className="mt-2 mb-5">
			<Row>
				<Col xs={12} lg={{ offset: 1, span: 10 }}>
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Row style={{ display: 'flex', alignItems: "center" }}>
							<Col xs={12} lg={4} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
								<Typography color={'#ce93d8'} variant="caption">Tournament Name</Typography>
								<Typography>{tournament?.name}</Typography>
							</Col>
							<Col xs={12} lg={4} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
								<Typography color={'#ce93d8'} variant="caption">Tournament Location</Typography>
								<Typography>{tournament?.location}</Typography>
							</Col>
							<Col xs={12} lg={4} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center border-end'}`}>
								<Typography color={'#ce93d8'} variant="caption">Tournament Rounds</Typography>
								<Typography>{tournament?.totalRounds}</Typography>
							</Col>
						</Row>

						{/* Tabs */}
						<Tabs
							value={activeTab}
							onChange={handleTabChange}
							indicatorColor="secondary"
							textColor="secondary"
							centered
							className="mt-4"
						>
							<Tab label="Players" />
							<Tab label="Rounds" />
							<Tab label="Standings" />
						</Tabs>
					</div>

					{/* Content based on the selected tab */}
					{activeTab === 0 &&
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Typography variant="h6" className="text-center">Tournament players</Typography>
						{tournament?.players && tournament?.players.length > 0 ? (
							<TableContainer component={Paper}>
								<Table sx={{ minWidth: 650 }} aria-label="simple table">
									<TableHead>
										<TableRow>
											<TableCell width={"10%"}></TableCell>
											<TableCell>Avatar</TableCell>
											<TableCell >Name</TableCell>
											<TableCell>Elo</TableCell>
										</TableRow>
									</TableHead>
									<TableBody>
										{tournament.players.map((player, index) => (
											<TableRow
												key={index}
												sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
											>
												<TableCell component="th" scope="row">
													{index + 1}.
												</TableCell>
												<TableCell>
													{player.playerImage ? (
														<Avatar aria-label="player" src={player.playerImage} sx={{ width: 56, height: 56, borderRadius: '10px', marginTop: '-5px' }}>
															P
														</Avatar>
													):(
														<Avatar aria-label="player" sx={{ width: 45, height: 45, borderRadius: '10px', marginTop: '-5px' }}>
															<AccountCircleIcon sx={{ width: 40, height: 40, color: 'white'}}/>
														</Avatar>
													)}
												</TableCell>
												<TableCell component="th" scope="row">
													{player.name}
												</TableCell>
												<TableCell>{player.elo}</TableCell>
											</TableRow>
										))}
									</TableBody>
								</Table>
							</TableContainer>
						) : (
							<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px'}}><p>No players.</p></div>
						)}
					</div>
					}
					{activeTab === 1 &&
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Typography variant="h6" className="text-center">Tournament rounds</Typography>
						{rounds && rounds.length > 0 ? (
							<Carousel activeIndex={index} onSelect={handleSelect} interval={null}>
								{rounds.map((round, index) => (
									<Carousel.Item key={index}>
										<div className="mt-5 text-center" style={{marginBottom: '100px'}}>
											{round.results.map((result, index2) => (
												<div className="d-flex justify-content-center align-items-center mt-4" key={index2}>
													<div className="me-5">
														{result.whitePlayerImage ? (
															<Avatar aria-label="player" src={result.whitePlayerImage} sx={{ width: 55, height: 55}} />
														):(
															<Avatar aria-label="player" sx={{ width: 55, height: 55, backgroundColor: 'transparent'}}>
																<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white'}}/>
															</Avatar>
														)}
														<span className={`${roundResultWinnerColor(result.result).whitePlayer}`}>{result.whitePlayerName}</span>
													</div>
													<div style={{minWidth: '100px'}}>
														<Tooltip key="details" title={roundResultTooltips(result.result, result.whitePlayerName, result.blackPlayerName)} arrow placement="bottom" componentsProps={componentsProps}>
															{roundResultLabels(result.result)}
														</Tooltip>
													</div>
													<div className="ms-5">
														{result.blackPlayerImage ? (
															<Avatar aria-label="player" src={result.blackPlayerImage} sx={{ width: 55, height: 55}} />
														):(
															<Avatar aria-label="player" sx={{ width: 55, height: 55, backgroundColor: 'transparent'}}>
																<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white'}}/>
															</Avatar>
														)}
														<span className={`${roundResultWinnerColor(result.result).blackPlayer}`}>{result.blackPlayerName}</span>
													</div>
												</div>
											))}
										</div>
									</Carousel.Item>
								))}
							</Carousel>
						):(
							<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px'}}><p>Round not started.</p></div>
						)}
					</div>
					}
					{activeTab === 2 &&
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						Standings
					</div>
					}
				</Col>
			</Row>
		</Container>
	);
}

export default Tournament;