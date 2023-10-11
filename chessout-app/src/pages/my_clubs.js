import React, { useEffect, useState } from 'react';
import { getStorage, ref, getDownloadURL } from "firebase/storage";
import {readMyClubs, readMyDefaultClub, getImage} from "utils/firebaseTools";
import {Container, Row, Col} from "react-bootstrap";
import { Typography, Paper, Grid, Box } from '@mui/material';
import {firebaseApp} from "config/firebase";
import ClubImage from 'assets/images/default_chess_club.jpg';
import Divider from "@mui/material/Divider";

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

		const sortedClubs = clubsWithImgSrc.slice().sort((a, b) => {
			if (a.isDefaultClub) {
				return -1; // a should come before b
			} else if (b.isDefaultClub) {
				return 1; // b should come before a
			}
			return 0; // no change in order
		});

		setClubs(sortedClubs);
	};

	useEffect(() => {
		if(props.firebaseUser){
			getMyClubs();
		}
	}, [props.firebaseUser]);

	return (
		<Container className="mt-3 mb-5">
			<Row>
				{clubs?.map((club) => (
					<Col xs={12} lg={3} key={club.clubId}>
						<Paper elevation={3} className={club.isDefaultClub ? 'selected-club': ''} style={{ padding: '20px', marginTop: '20px', minHeight: '530px' }} key={club.clubId}>
							<div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '220px' }}>
								{club.img_src ? (
									<img src={club.img_src} className="b-r-sm" alt="Club Image" style={{ height: '100%', width: '100%' }} />
								) : (
									<img src={ClubImage} className="b-r-sm" alt="Club Image" style={{ height: '100%', width: '100%'}} />
								)}
							</div>
							<Grid container spacing={2}>
								<Grid item xs={12}>
									<Typography variant="h4" gutterBottom className="text-center mt-2">
										{club.name}
									</Typography>
									<Divider color={"white"} />
								</Grid>
								<Grid item xs={12}>
									<div className="d-flex justify-content-between">
										<Typography variant="body2">
											City
										</Typography>
										<Typography variant="body2">
											{club.city}
										</Typography>
									</div>
									<div className="d-flex justify-content-between">
										<Typography variant="body2">
											Country
										</Typography>
										<Typography variant="body2">
											{club.country}
										</Typography>
									</div>
									<div className="d-flex justify-content-between">
										<Typography variant="body2">
											Created
										</Typography>
										<Typography variant="body2">
											{new Date(club.dateCreated.timestamp).toLocaleDateString()}
										</Typography>
									</div>
									<Divider color={"white"} className="mt-3" />
								</Grid>
								<Grid item xs={12}>
									<Typography variant="body2" className="text-justified">
										{club.description}
									</Typography>
								</Grid>
							</Grid>
						</Paper>
					</Col>
				))}
			</Row>
		</Container>
	);
}

export default MyClubs;