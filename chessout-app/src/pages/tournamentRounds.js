import React, {useEffect, useState} from "react";
import { Link, useParams } from 'react-router-dom';
import {getTournament, getTournamentRoundGamesDecoded, getTournamentRoundGames} from "utils/firebaseTools";
import {Col, Container, Row} from "react-bootstrap";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "@firebase/storage";
import { Avatar, Typography, ButtonGroup, Button as MuiButton } from "@mui/material";
import Tooltip from "@mui/material/Tooltip";
import Fade from "@mui/material/Fade";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import Carousel from 'react-bootstrap/Carousel';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';

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

function roundResultWinnerColor(result){
	switch (result) {
		case 0: return {whitePlayer: '', blackPlayer: ''};
		case 1: return {whitePlayer: 'text-light-success', blackPlayer: ''};
		case 2: return {whitePlayer: '', blackPlayer: 'text-light-success'};
		case 3: return {whitePlayer: 'text-light-success', blackPlayer: 'text-light-success'};
		case 5: return {whitePlayer: 'text-light-success', blackPlayer: ''};
		case 6: return {whitePlayer: '', blackPlayer: 'text-light-success'};
		case 7: return {whitePlayer: '', blackPlayer: ''};
		case 8: return {whitePlayer: '', blackPlayer: ''};
	}
}

function roundResultLabels(result){
	switch (result) {
		case 0: return (<Typography variant="body1" fontSize="20px"><span className="font-bold">---</span></Typography>);
		case 1: return (<Typography variant="body1" fontSize="17px"><span className="text-light-success font-bold">1</span> - 0</Typography>);
		case 2: return (<Typography variant="body1" fontSize="17px">0 - <span className="text-light-success font-bold">1</span></Typography>);
		case 3: return (<Typography variant="body1" fontSize="17px"><span className="text-light-success font-bold">1/2 - 1/2</span></Typography>);
		case 5: return (<Typography variant="body1" fontSize="17px"><span className="text-light-success font-bold">1ff</span> - 0</Typography>);
		case 6: return (<Typography variant="body1" fontSize="17px">0 - <span className="text-light-success font-bold">1ff</span></Typography>);
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

function TournamentRounds(props) {
	const { tournamentId, activeRoundId } = useParams();
	const storage = getStorage(firebaseApp);
	const [tournament, setTournament] = useState(null);
	const [rounds, setRounds] = useState(null);

	//Get tournament
	const getMyTournament = async () => {
		const defaultClub = await props.getMyDefaultClub();
		const myTournamentData = await getTournament(defaultClub?.clubKey, tournamentId);

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
							whitePlayerName: result.whitePlayer?.name,
							whitePlayerImage: result.whitePlayer?.profilePictureUri ? await getDownloadURL(ref(storage, result.whitePlayer.profilePictureUri)) : null,
							blackPlayerName: result.blackPlayer?.name,
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

		setTournament(myTournamentData);
		setRounds(roundsInfo);
	};

	useEffect(() => {
		if (props.firebaseUser) {
			getMyTournament();
		}
	}, [props.firebaseUser]);

	const [index, setIndex] = useState(parseInt(activeRoundId, 10));
	const handleSelect = (selectedIndex) => {
		setIndex(selectedIndex);
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
									variant="text"
									color={'success'}
									className="me-2"
									style={{
										borderBottom: ' 1px solid #66bb6a',
										borderRadius: 0,
										borderRight: 'none',
										backgroundColor: 'transparent'
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
									component={Link}
									to={`/tournament-join-requests/${tournamentId}`}
									variant="text"
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

					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Typography variant="h6" className="text-center">Tournament Rounds</Typography>
						{rounds && rounds.length > 0 ? (
							<Carousel activeIndex={index} onSelect={handleSelect} interval={null} wrap={false}>
								{rounds.map((round, index) => (
									<Carousel.Item key={index}>
										<Typography variant="subtitle2" className="text-center mt-2">Round {round.round}</Typography>
										<Typography variant="subtitle2" className="text-center text-light-success">Round progress {round.completedGames} / {round.totalGames}</Typography>
										<div className="mt-5" style={{marginBottom: '100px'}}>
											<TableContainer className="custom-table-container">
												<Table aria-label="simple table" className="custom-table">
													<TableBody>
													{round.results.map((result, index2) => (
														<TableRow
															key={index2}
														>
															<TableCell width={props.isMobile ? '' : '45%'} align="center" style={props.isMobile ? {} : { paddingLeft: '30%' }}>
															<div className="d-flex justify-content-center">
																	{result.whitePlayerImage ? (
																		<Avatar aria-label="player" src={result.whitePlayerImage} sx={{ width: 55, height: 55}} />
																	):(
																		<Avatar aria-label="player" sx={{ width: 55, height: 55, backgroundColor: 'transparent'}}>
																			<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white'}}/>
																		</Avatar>
																	)}
																</div>
																<span className={`${roundResultWinnerColor(result.result).whitePlayer}`}>{result.whitePlayerName}</span>
															</TableCell>
															<TableCell align="center">
																<Tooltip key="details" title={roundResultTooltips(result.result, result.whitePlayerName, result.blackPlayerName)} arrow placement="bottom" componentsProps={componentsProps}>
																	{roundResultLabels(result.result)}
																</Tooltip>
															</TableCell>
															<TableCell width={props.isMobile ? '' : '45%'} align="center" style={props.isMobile ? {} : { paddingRight: '30%' }}>
																<div className="d-flex justify-content-center">
																	{result.blackPlayerImage ? (
																		<Avatar aria-label="player" src={result.blackPlayerImage} sx={{ width: 55, height: 55}} />
																	):(
																		<Avatar aria-label="player" sx={{ width: 55, height: 55, backgroundColor: 'transparent'}}>
																			<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white'}}/>
																		</Avatar>
																	)}
																</div>
																<span className={`${roundResultWinnerColor(result.result).blackPlayer}`}>{result.blackPlayerName}</span>
															</TableCell>
														</TableRow>

													))}
													</TableBody>
												</Table>
											</TableContainer>
										</div>
									</Carousel.Item>
								))}
							</Carousel>
						):(
							<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px'}}><p>Round not started.</p></div>
						)}
					</div>
				</Col>
			</Row>
		</Container>
	);
}

export default TournamentRounds;