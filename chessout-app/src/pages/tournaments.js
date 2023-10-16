import React, {useEffect, useState} from "react";
import {getTournaments, getClub, getClubProfilePicture} from "utils/firebaseTools";
import {Col, Container, Row} from "react-bootstrap";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "@firebase/storage";
import { Avatar, Typography, IconButton } from "@mui/material";
import Tooltip from "@mui/material/Tooltip";
import Fade from "@mui/material/Fade";
import DefaultClubImage from 'assets/images/default_chess_club.jpg';
import DoubleArrowIcon from '@mui/icons-material/DoubleArrow';
import { Link } from 'react-router-dom';

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

function Tournaments(props) {
	const storage = getStorage(firebaseApp);
	const currentTimestamp = Date.now();
	const [tournaments, setTournaments] = useState(null);

	//Get tournaments
	const getMyTournaments = async () => {
		const defaultClub = await props.getMyDefaultClub();
		const myTournamentsData = await getTournaments(defaultClub?.clubKey);
		const myTournamentsArray = Object.values(myTournamentsData);

		//get the image of club
		let clubImage = null;
		const clubImageData = await getClubProfilePicture(defaultClub?.clubKey);
		if (clubImageData?.uploadComplete) {
			clubImage = await getDownloadURL(ref(storage, clubImageData.stringUri));
		}else{
			clubImage = DefaultClubImage;
		}

		const tournamentsWithDetails = await Promise.all(myTournamentsArray.map(async (tournament) => {
			//get club extra info
			const clubInfo = await getClub(tournament.clubId);

			return {
				...tournament,
				clubInfo: clubInfo,
				clubImage: clubImage,
			};
		}));
		tournamentsWithDetails.sort((a, b) => b.dateCreated.timestamp - a.dateCreated.timestamp);
		setTournaments(tournamentsWithDetails);
	};

	useEffect(() => {
		if (props.firebaseUser) {
			getMyTournaments();
		}
	}, [props.firebaseUser]);

	return(
		<Container className="mt-2 mb-5">
			<Row>
				<Col xs={12} lg={{ offset: 1, span: 10 }}>
					{tournaments && tournaments.length > 0? (
						<React.Fragment>
							{tournaments.map((tournament) => (
								<div key={tournament.tournamentId} className="px-3 py-3 b-r-sm mt-4" style={{backgroundImage: "linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))"}}>
									<Row style={{ display: 'flex', alignItems: "center" }}>
										<Col xs={3} lg={1} className="mt-1">
											<Avatar aria-label="User" src={tournament.clubImage} sx={{ width: 56, height: 56, borderRadius: '10px', marginTop: '-5px' }}>
												C
											</Avatar>
										</Col>
										<Col xs={12} lg={2} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
											<Typography className="text-green-400" variant="caption" >Club Name</Typography>
											<Typography>{tournament.clubInfo.name}</Typography>
										</Col>
										<Col xs={12} lg={3} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
											<Typography className="text-green-400" variant="caption">Tournament Name</Typography>
											<Typography>{tournament.name}</Typography>
										</Col>
										<Col xs={12} lg={3} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center'}`}>
											<Typography className="text-green-400" variant="caption">Location</Typography>
											<Typography>{tournament.location}</Typography>
										</Col>
										<Col xs={12} lg={2} className={`border-start ${props.isMobile ? 'mt-3' : 'text-center border-end'}`}>
											<Typography className="text-green-400" variant="caption">Rounds</Typography>
											<Typography>{tournament.totalRounds}</Typography>
										</Col>
										<Col xs={12} lg={1} className={`text-center`}>
											<Tooltip key="details" title="View more details" arrow placement="bottom" componentsProps={componentsProps}>
												<Link to={`/tournament-players/${tournament.tournamentId}`}>
													<IconButton aria-label="details" size="small" className="text-light">
														{props.isMobile ? 'More details' : ''} <DoubleArrowIcon fontSize="small" />
													</IconButton>
												</Link>
											</Tooltip>
										</Col>
									</Row>
							</div>
							))}
						</React.Fragment>
					) : (
						<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '25px', paddingBottom: '10px'}}><p>No tournaments available.</p></div>
					)}
				</Col>
			</Row>
		</Container>
	);
}

export default Tournaments;