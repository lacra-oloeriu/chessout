import React, {useEffect, useState} from "react";
import { Link, useParams } from 'react-router-dom';
import {getTournament, getTournamentPlayers, getTournamentRoundGamesDecoded, getTournamentStandings} from "utils/firebaseTools";
import {Col, Container, Row} from "react-bootstrap";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "@firebase/storage";
import { Avatar, Typography, ButtonGroup, Button as MuiButton } from "@mui/material";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import Carousel from 'react-bootstrap/Carousel';

function TournamentStandings(props) {
	const { tournamentId } = useParams();
	const storage = getStorage(firebaseApp);
	const [tournament, setTournament] = useState(null);
	const [standings, setStandings] = useState(null);
	const [emptyStandings, setEmptyStandings] = useState(true);

	//Get tournament
	const getMyTournament = async () => {
		const defaultClub = await props.getMyDefaultClub();
		const myTournamentData = await getTournament(defaultClub?.clubKey, tournamentId);

		// get tournament players
		const tournamentPlayers = await getTournamentPlayers(defaultClub?.clubKey, tournamentId);
		myTournamentData.playersCount = tournamentPlayers ? Object.keys(tournamentPlayers).length : 0;

		// get tournament standings
		const standingsInfo = [];
		for (let j = 1; j <= myTournamentData?.totalRounds; j++){
			const tournamentRoundGamesDecodedData = await getTournamentRoundGamesDecoded(defaultClub?.clubKey, tournamentId, j);
			if (tournamentRoundGamesDecodedData) {

				const roundStandings = [];
				for (let k = 1; k <= myTournamentData?.playersCount; k++){
					const tournamentStandings = await getTournamentStandings(defaultClub?.clubKey, tournamentId, j, k);
					if(tournamentStandings){
						tournamentStandings.playerImage = tournamentStandings?.profilePictureUri ? await getDownloadURL(ref(storage, tournamentStandings.profilePictureUri)) : null;
						roundStandings.push(tournamentStandings);
					}
				}

				standingsInfo.push({
					round: j,
					standings: roundStandings,
				});
			}
		}

		standingsInfo.map(item =>{
			if(Object.keys(item.standings).length){
				setEmptyStandings(false);
			}
		});

		setTournament(myTournamentData);
		setStandings(standingsInfo);
	};

	useEffect(() => {
		if (props.firebaseUser) {
			getMyTournament();
		}
	}, [props.firebaseUser]);

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
								<Typography color={'#66bb6a'} variant="caption">Tournament Name</Typography>
								<Typography>{tournament?.name}</Typography>
							</Col>
							<Col xs={12} lg={4} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
								<Typography color={'#66bb6a'} variant="caption">Tournament Location</Typography>
								<Typography>{tournament?.location}</Typography>
							</Col>
							<Col xs={12} lg={4} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center border-end'}`}>
								<Typography color={'#66bb6a'} variant="caption">Tournament Rounds</Typography>
								<Typography>{tournament?.totalRounds}</Typography>
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
									variant="text"
									className="me-2"
									to={`/tournament-players/${tournamentId}`}
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
									variant="text"
									color={'success'}
									className="ms-2"
									style={{
										borderBottom: ' 1px solid #66bb6a',
										borderRadius: 0,
										borderRight: 'none',
										backgroundColor: 'transparent'
									}}
								>
									Standings
								</MuiButton>
							</ButtonGroup>
						</div>
					</div>
				</Col>
			</Row>
			<Row>
				<Col xs={12}>
					<div className="p-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
						<Typography variant="h6" className="text-center">Tournament Standings</Typography>
						{standings && standings.length > 0 && !emptyStandings ? (
							<Carousel activeIndex={index} onSelect={handleSelect} interval={null} wrap={false}>
								{standings.map((round, index) => (
									<Carousel.Item key={index}>
										<Typography variant="subtitle2" className="text-center text-light-success mt-2">Round {round.round}</Typography>
										<div className="mt-5" style={{marginBottom: '100px'}}>
											<TableContainer>
												<Table aria-label="simple table">
													<TableHead>
														<TableRow>
															<TableCell align={"center"} width={"7%"} style={{color: '#66bb6a'}}>Rank</TableCell>
															<TableCell align={"center"} width={"8%"} style={{color: '#66bb6a'}}>SNo</TableCell>
															<TableCell align={"center"} width={"30%"} style={{color: '#66bb6a'}}>Player</TableCell>
															<TableCell align={"center"} style={{color: '#66bb6a'}}>Rating</TableCell>
															<TableCell align={"center"} style={{color: '#66bb6a'}}>Points</TableCell>
															<TableCell align={"center"} width={"15%"} style={{color: '#66bb6a'}}>Buchholz Points</TableCell>
														</TableRow>
													</TableHead>
													<TableBody>
														{round.standings.map((standing, index2) => (
															<TableRow key={index2}>
																<TableCell align={"center"}>{standing.rankNumber}</TableCell>
																<TableCell align={"center"}>{standing.tournamentInitialOrder}</TableCell>
																<TableCell align={"center"}>
																	<div className="d-flex align-items-center justify-content-center">
																		<div className="d-inline-flex">
																			{standing.playerImage ? (
																				<Avatar aria-label="player" src={standing.playerImage} sx={{ width: 55, height: 55}}/>
																			):(
																				<Avatar aria-label="player" sx={{ width: 55, height: 55, backgroundColor: 'transparent'}}>
																					<AccountCircleIcon sx={{ width: 60, height: 60, color: 'white'}}/>
																				</Avatar>
																			)}
																		</div>
																		<span className="d-inline-flex ms-4" style={{minWidth: '100px'}}>{standing.playerName}</span>
																	</div>
																</TableCell>
																<TableCell align={"center"}>{standing.elo}</TableCell>
																<TableCell align={"center"}>{standing.points.toFixed(1)}</TableCell>
																<TableCell align={"center"}>{standing.buchholzPoints.toFixed(1)}</TableCell>
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
							<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '20px', paddingBottom: '5px'}}><p>No standings available.</p></div>
						)}
					</div>
				</Col>
			</Row>
		</Container>
	);
}

export default TournamentStandings;