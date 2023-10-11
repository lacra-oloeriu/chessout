import React, { useEffect, useState } from 'react';
import { getStorage, ref, getDownloadURL } from "firebase/storage";
import {readMyClubs, readMyDefaultClub, getImage} from "utils/firebaseTools";
import {Container, Row, Col} from "react-bootstrap";
import { Typography, Paper, Grid, Box } from '@mui/material';
import {firebaseApp} from "config/firebase";

function MyClubs(props) {
	const storage = getStorage(firebaseApp);
	const [clubs, setClubs] = useState([]);

	const getMyClubs = async () => {
		const myClubs = await readMyClubs({ uid: props.firebaseUser.uid });

		// Create a new array of clubs with img_src added
		const clubsWithImgSrc = await Promise.all(myClubs.map(async (club) => {
			if (club.picture) {
				const new_image = await getDownloadURL(ref(storage, club.picture.stringUri));
				return { ...club, img_src: new_image };
			} else {
				return { ...club, img_src: null };
			}
		}));

		setClubs(clubsWithImgSrc);
	};

	useEffect(() => {
		if(props.firebaseUser){
			getMyClubs();
		}
	}, [props.firebaseUser]);

	return (
		<Container>
			<Row>
				{clubs?.map((club) => (
					<Col xs={12} lg={4} key={club.clubId}>
						<Paper elevation={3} style={{ padding: '20px', marginTop: '20px' }} key={club.clubId}>
							{club.img_src ? (
								<img src={club.img_src} alt="Club Image" style={{ maxWidth: '100%', maxHeight: 'auto' }} />
							):(
								<img src={club.img_src} alt="Club Image" style={{ maxWidth: '100%', maxHeight: 'auto' }} />
							)}
							<Grid container spacing={2}>
								<Grid item xs={12}>
									<Typography variant="h4" gutterBottom>
										{club.name}
									</Typography>
								</Grid>
								<Grid item xs={12} sm={6}>
									<Typography variant="subtitle1">
										Club ID: {club.clubId}
									</Typography>
									<Typography variant="subtitle1">
										Country: {club.country}
									</Typography>
									<Typography variant="subtitle1">
										City: {club.city}
									</Typography>
									<Typography variant="subtitle1">
										Description: {club.description}
									</Typography>
								</Grid>
								<Grid item xs={12} sm={6}>
									<Typography variant="subtitle1">
										Email: {club.email}
									</Typography>
									<Typography variant="subtitle1">
										Short Name: {club.shortName}
									</Typography>
									<Typography variant="subtitle1">
										Home Page: {club.homePage}
									</Typography>
									<Typography variant="subtitle1">
										Created: {new Date(club.dateCreated.timestamp).toLocaleString()}
									</Typography>
								</Grid>
							</Grid>
							{club.isDefaultClub && (
								<Box mt={2}>
									<Typography variant="subtitle1" color="primary">
										Default Club
									</Typography>
								</Box>
							)}
						</Paper>
					</Col>
				))}
			</Row>
		</Container>
	);
}

export default MyClubs;