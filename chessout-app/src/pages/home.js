import React, { useEffect, useState } from "react";
import { getUserHomePosts, getPostsLikes, getPostChat, getUserProfilePicture, getClub, getTournament, getTournamentPlayers, getTournamentRoundGamesDecoded } from "utils/firebaseTools";
import { Container, Row, Col } from "react-bootstrap";
import UserPost from "components/userPost";
import TournamentPost from "components/tournamentPost";
import {firebaseApp} from "config/firebase";
import {getDownloadURL, getStorage, ref} from "@firebase/storage";
import ClubImage from 'assets/images/default_chess_club.jpg';
import MenuItem from "@mui/material/MenuItem";

function Home(props) {
	const storage = getStorage(firebaseApp);
	const currentTimestamp = Date.now();
	const [posts, setPosts] = useState(null);

	const getMyPosts = async () => {
		const myPosts = await getUserHomePosts(props.firebaseUser.uid);
		let myPostsArray = myPosts ? Object.values(myPosts) : [];

		// Create a new array of clubs with extra details
		const postsWithDetails = await Promise.all(myPostsArray.map(async (post) => {
			if(post) {
				// get the image of the post
				let new_image = null;
				if (post.pictures) {
					try {
						new_image = await getDownloadURL(ref(storage, post.pictures[0].stringUri));
					} catch (error) {
						new_image = null;
					}
				}

				//get the likes data and the likes amount of the post
				const likesData = await getPostsLikes('', post.clubId, post.postId)
				const likes = likesData ? Object.values(likesData) : [];
				let likesCount = 0;
				if (likes) {
					likesCount = Object.keys(likes).length;
				}

				//check if the logged in user like the post
				let isLikedByCurrentUser = false;
				if (likesData) {
					const likesKeys = Object.keys(likesData);
					isLikedByCurrentUser = likesKeys.includes(props.firebaseUser.uid);
				}

				// get the comments data and the comments amount of the post
				const commentsData = await getPostChat(post.postId);
				const comments = commentsData ? Object.values(commentsData) : [];
				if (comments) {
					for (const comment of comments) {
						comment.userImage = await getUserProfilePicture(comment.userId);
						if (comment.userImage.uploadComplete) {
							try {
								comment.userImage.img_src = await getDownloadURL(ref(storage, comment.userImage.stringUri));
							} catch (error) {
								comment.userImage.img_src = null;
							}
						} else {
							comment.userImage.img_src = comment.userImage.stringUri;
						}

						const commentTimeDiff = currentTimestamp - comment.timeStampCreate;

						if (commentTimeDiff <= 60000) {
							comment.time = '1 min';
						} else if (commentTimeDiff <= 3600000) {
							const minutes = Math.floor(commentTimeDiff / 60000);
							comment.time = `${minutes}min`;
						} else if (commentTimeDiff <= 86400000) {
							const hours = Math.floor(commentTimeDiff / 3600000);
							comment.time = `${hours}h`;
						} else if (commentTimeDiff <= 604800000) {
							const days = Math.floor(commentTimeDiff / 86400000);
							comment.time = `${days}d`;
						} else if (commentTimeDiff <= 2419200000) {
							const weeks = Math.floor(commentTimeDiff / 604800000);
							comment.time = `${weeks > 1 ? weeks : '1'} week${weeks > 1 ? 's' : ''}`;
						} else if (commentTimeDiff <= 29030400000) {
							const months = Math.floor(commentTimeDiff / 2419200000);
							comment.time = `${months > 1 ? months : '1'} month${months > 1 ? 's' : ''}`;
						} else {
							const years = Math.floor(commentTimeDiff / 29030400000);
							comment.time = `${years > 1 ? years : '1'} year${years > 1 ? 's' : ''}`;
						}
					}
				}
				let commentsCount = 0;
				if (comments) {
					commentsCount = Object.keys(comments).length;
				}

				//get the image of club
				let new_club_image = null;
				if (post.clubPictureUrl) {
					try {
						new_club_image = await getDownloadURL(ref(storage, post.clubPictureUrl));
					} catch (error) {
						new_club_image = ClubImage;
					}
				} else {
					new_club_image = ClubImage;
				}

				//get club extra info
				const clubInfo = await getClub(post.clubId);

				//get tournament details
				let tournamentCreation = null;
				let tournamentCreationPlayersCount = null;
				if (post.tournamentId) {
					const tournamentCreationData = await getTournament(post.clubId, post.tournamentId);
					const tournamentCreationPlayers = await getTournamentPlayers(post.clubId, post.tournamentId);
					if (tournamentCreationPlayers) {
						tournamentCreationPlayersCount = Object.keys(tournamentCreationPlayers).length;
					}
					tournamentCreation = {
						name: tournamentCreationData.name ? tournamentCreationData.name : '',
						location: tournamentCreationData.location ? tournamentCreationData.location : '',
						totalRounds: tournamentCreationData.totalRounds ? tournamentCreationData.totalRounds : 0,
						playersCount: tournamentCreationPlayersCount ? tournamentCreationPlayersCount : 0,
						players: tournamentCreationPlayers ? tournamentCreationPlayers : [],
					};
				}

				//get tournament pairings details
				let tournamentPairings = {
					completedGames: 0,
					totalGames: 0
				};
				if (post.postType === 'TOURNAMENT_PAIRINGS_AVAILABLE' && post.tournamentId) {
					const tournamentPairingsData = await getTournamentRoundGamesDecoded(post.clubId, post.tournamentId, post.roundId);
					if (tournamentPairingsData) {
						tournamentPairings = tournamentPairingsData;
					}
				}

				return {
					...post,
					img_src: new_image,
					likesCount: likesCount,
					likes: likes,
					isLikedByCurrentUser: isLikedByCurrentUser,
					commentsCount: commentsCount,
					comments: comments,
					clubImage: new_club_image,
					clubName: clubInfo.name,
					tournamentCreation: tournamentCreation,
					tournamentPairings: tournamentPairings
				};
			}
		}));

		// Calculate and format the time difference for each post
		postsWithDetails.forEach((post) => {
			const timeDifference = currentTimestamp - post.dateCreated.timestamp;

			if (timeDifference <= 60000) {
				post.time = 'a minute ago';
			} else if (timeDifference <= 3600000) {
				const minutes = Math.floor(timeDifference / 60000);
				post.time = `${minutes > 1 ? minutes : 'a'} minute${minutes > 1 ? 's' : ''} ago`;
			} else if (timeDifference <= 86400000) {
				const hours = Math.floor(timeDifference / 3600000);
				post.time = `${hours > 1 ? hours : 'an'} hour${hours > 1 ? 's' : ''} ago`;
			} else if (timeDifference <= 604800000) {
				const days = Math.floor(timeDifference / 86400000);
				post.time = `${days > 1 ? days : 'a'} day${days > 1 ? 's' : ''} ago`;
			} else if (timeDifference <= 2419200000) {
				const weeks = Math.floor(timeDifference / 604800000);
				post.time = `${weeks > 1 ? weeks : 'a'} week${weeks > 1 ? 's' : ''} ago`;
			} else if (timeDifference <= 29030400000) {
				const months = Math.floor(timeDifference / 2419200000);
				post.time = `${months > 1 ? months : 'a'} month${months > 1 ? 's' : ''} ago`;
			} else {
				const years = Math.floor(timeDifference / 29030400000);
				post.time = `${years > 1 ? years : 'a'} year${years > 1 ? 's' : ''} ago`;
			}
		});

		// Sort the posts by dateCreated.timestamp in descending order
		postsWithDetails.sort((a, b) => b.dateCreated.timestamp - a.dateCreated.timestamp);
		setPosts(postsWithDetails);
	};

	useEffect(() => {
		if (props.firebaseUser) {
			getMyPosts();
		}
	}, [props.firebaseUser]);


	return (
		<Container className="mt-2 mb-5">
			<Row>
				<Col xs={12} lg={{ offset: 3, span: 6 }}>
					{posts && posts.length > 0? (
						<Row>
							{posts.map((post) => (
								<React.Fragment key={post.postId}>
									{post.postType === "USER_POST" && (
										<Col xs={12} className="mt-4">
											<UserPost
												image={post.img_src}
												title={post.message}
												user={post.userName}
												time={post.time}
												avatar={post.userPictureUrl}
												likesCount={post.likesCount}
												likes={post.likes}
												isLikedByCurrentUser={post.isLikedByCurrentUser}
												commentsCount={post.commentsCount}
												comments={post.comments}
											/>
										</Col>
									)}
									{post.postType === "TOURNAMENT_CREATED" && (
										<Col xs={12} className="mt-4">
											<TournamentPost
												image={post.img_src}
												title={"New tournament created"}
												user={post.clubName}
												time={post.time}
												avatar={post.clubImage}
												likesCount={post.likesCount}
												likes={post.likes}
												isLikedByCurrentUser={post.isLikedByCurrentUser}
												commentsCount={post.commentsCount}
												comments={post.comments}
												tName={post.tournamentCreation.name}
												tLocation={post.tournamentCreation.location}
												tPlayersCount={post.tournamentCreation.playersCount}
												isPairingsType={false}
												tournamentId={post.tournamentId}
												goToLabel="View Tournament"
												goToLink={`/tournament-players/${post.tournamentId}`}
											/>
										</Col>
									)}
									{post.postType === "TOURNAMENT_PAIRINGS_AVAILABLE" && (
										<Col xs={12} className="mt-4">
											<TournamentPost
												image={post.img_src}
												title={`Round ${post.roundId} pairings available`}
												user={post.clubName}
												time={post.time}
												avatar={post.clubImage}
												likesCount={post.likesCount}
												likes={post.likes}
												isLikedByCurrentUser={post.isLikedByCurrentUser}
												commentsCount={post.commentsCount}
												comments={post.comments}
												tName={post.tournamentCreation.name}
												tLocation={post.tournamentCreation.location}
												tPlayersCount={post.tournamentCreation.playersCount}
												isPairingsType={true}
												completedGames={post.tournamentPairings?.completedGames}
												totalGames={post.tournamentPairings?.totalGames}
												roundId={post.roundId}
												goToLabel="View Pairings"
												goToLink={`/tournament-rounds/${post.tournamentId}/${post.roundId - 1}`}
											/>
										</Col>
									)}
								</React.Fragment>
							))}
						</Row>
					) : (
						<div className="text-center align-content-center b-r-sm mt-5" style={{backgroundColor: "#2f2f2f", paddingTop: '25px', paddingBottom: '10px'}}><p>No posts available.</p></div>
					)}
				</Col>
			</Row>
		</Container>
	);
}

export default Home;
