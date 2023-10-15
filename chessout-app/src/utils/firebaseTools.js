import {get, getDatabase, ref} from "firebase/database";
import {getStorage} from "firebase/storage";
import {firebaseApp} from "config/firebase";

const storage = getStorage(firebaseApp);

const USER_SETTINGS = 'userSettings';
const USER_KEY = '$userKey';
const DEFAULT_CLUB = 'defaultClub';
const MY_CLUBS = 'myClubs';
const CLUB_POSTS = 'clubPosts';
const POST_LIKES = "postLikes";
const USER_STREAM = 'userStream';
const POST_ITEMS = 'postItems';
const CHAT = "chat";
const CHAT_ITEMS = "chatItems";
const USER_PUBLIC_INFO = "userPublicInfo";
const PROFILE_PICTURE = "profilePicture";
const CLUBS = 'clubs';
const TOURNAMENTS = 'tournaments';
const TOURNAMENT_PLAYERS = 'tournamentPlayers';
const TOURNAMENT_ROUNDS = 'tournamentRounds';
const GAMES = 'games';

export async function readMyDefaultClub(userId) {
	const LOCATION_DEFAULT_CLUB = `${USER_SETTINGS}/${userId}/${DEFAULT_CLUB}`;
	const defaultClubData = await get(ref(getDatabase(firebaseApp), LOCATION_DEFAULT_CLUB));

	if (defaultClubData.exists()) {
		return defaultClubData.val();
	} else {
		return null;
	}
}

export async function readMyClubs(user) {
	const LOCATION_DEFAULT_CLUB = `${USER_SETTINGS}/${USER_KEY}/${DEFAULT_CLUB}`;
	const LOCATION_MY_CLUBS = `${USER_SETTINGS}/${USER_KEY}/${MY_CLUBS}`;

	const locationDefaultClub = LOCATION_DEFAULT_CLUB.replace(USER_KEY, user.uid);
	const defaultClub = await get(ref(getDatabase(firebaseApp), locationDefaultClub));

	const locationMyClubs = LOCATION_MY_CLUBS.replace(USER_KEY, user.uid);
	const myClubsSnapshot = await get(ref(getDatabase(firebaseApp), locationMyClubs));

	const myClubs = [];
	if(myClubsSnapshot){
		myClubsSnapshot.forEach(club => {
			const clubData = club.val();
			const isDefaultClub = defaultClub && defaultClub.val().clubKey === clubData.clubId;
			myClubs.push({ ...clubData, isDefaultClub });
		})
	}

	return myClubs;
}

export async function getUserHomePosts(user) {
	const LOCATION_USER_STREAM_POSTS = `${USER_SETTINGS}/${USER_KEY}/${USER_STREAM}/${POST_ITEMS}`;
	let postsLocation = LOCATION_USER_STREAM_POSTS.replace(USER_KEY, user);

	const userHomePosts = await get(ref(getDatabase(firebaseApp), postsLocation));
	if (userHomePosts.exists()) {
		return userHomePosts.val();
	} else {
		return null;
	}
}

export async function getPostsLikes(user, club, post) {
	const CLUB_POST_LIKE = `${CLUB_POSTS}/${club}/${POST_LIKES}/${post}/${user}`;
	const postsLikesData = await get(ref(getDatabase(firebaseApp), CLUB_POST_LIKE));

	if (postsLikesData.exists()) {
		return postsLikesData.val();
	} else {
		return null;
	}
}

export function newGenericInstance(locationType, chatClubId, chatTournamentId, chatRoundId, userId) {
	const chat = {
		locationType,
		chatClubId,
		chatTournamentId,
		chatRoundId,
		userId,
		timeStampCreate: new Date().getTime(),
		timeStampEdit: new Date().getTime(),
	};
	return chat;
}

export function newTextInstance(locationType, chatClubId, chatTournamentId, chatRoundId, itemType, userId, userName, textValue) {
	const chat = newGenericInstance(locationType, chatClubId, chatTournamentId, chatRoundId, userId);
	chat.itemType = itemType;
	chat.userId = userId;
	chat.userName = userName;
	chat.textValue = textValue;
	return chat;
}

export async function getTournamentChat(tournamentId, roundId) {
	const chatFolderKey = `${tournamentId}-round-${roundId}`;
	const CHAT_LOCATION = `${CHAT}/${chatFolderKey}/${CHAT_ITEMS}`;
	const chatData = await get(ref(getDatabase(firebaseApp), CHAT_LOCATION));

	if (chatData.exists()) {
		return chatData.val();
	} else {
		return null;
	}
}

export async function getPostChat(postId) {
	const CHAT_LOCATION = `${CHAT}/${postId}/${CHAT_ITEMS}`;
	const chatData = await get(ref(getDatabase(firebaseApp), CHAT_LOCATION));

	if (chatData.exists()) {
		return chatData.val();
	} else {
		return null;
	}
}

export async function getUserProfilePicture(userId) {
	const LOCATION_USER_PROFILE_PICTURE = `${USER_PUBLIC_INFO}/${userId}/${PROFILE_PICTURE}`;
	const imageData = await get(ref(getDatabase(firebaseApp), LOCATION_USER_PROFILE_PICTURE));

	if (imageData.exists()) {
		return imageData.val();
	} else {
		return null;
	}
}

export async function getClub(clubId) {
	const LOCATION_CLUB = `${CLUBS}/${clubId}`;
	const clubData = await get(ref(getDatabase(firebaseApp), LOCATION_CLUB));

	if (clubData.exists()) {
		return clubData.val();
	} else {
		return null;
	}
}

export async function getTournament(clubId, tournamentId) {
	const LOCATION_TOURNAMENT = `${TOURNAMENTS}/${clubId}/${tournamentId}`;
	const tournamentData = await get(ref(getDatabase(firebaseApp), LOCATION_TOURNAMENT));

	if (tournamentData.exists()) {
		return tournamentData.val();
	} else {
		return null;
	}
}

export async function getTournamentPlayers(clubId, tournamentId) {
	const LOCATION_TOURNAMENT_PLAYERS = `${TOURNAMENT_PLAYERS}/${clubId}/${tournamentId}`;
	const tournamentPlayersData = await get(ref(getDatabase(firebaseApp), LOCATION_TOURNAMENT_PLAYERS));

	if (tournamentPlayersData.exists()) {
		return tournamentPlayersData.val();
	} else {
		return null;
	}
}

function decodeGames(games) {
	let totalGames = 0;
	let completedGames = 0;

	if (games == null) {
		return {completedGames, totalGames};
	}
	for (const [key, game] of Object.entries(games)) {
		if (game != null) {
			let result = game.result;
			if (result != null) {
				totalGames += 1;
			}
			if (result != null && result != '0') {
				completedGames += 1;
			}
		}
	}
	return {completedGames, totalGames};
}

export async function getTournamentRoundGames(clubId, tournamentId, roundId) {
	const LOCATION_ROUND_GAMES = `${TOURNAMENT_ROUNDS}/${clubId}/${tournamentId}/${roundId}/${GAMES}`;
	const roundData = await get(ref(getDatabase(firebaseApp), LOCATION_ROUND_GAMES));

	if (roundData.exists()) {
		return roundData.val();
	} else {
		return null;
	}
}

export async function getTournamentRoundGamesDecoded(clubId, tournamentId, roundId) {
	const LOCATION_ROUND_GAMES = `${TOURNAMENT_ROUNDS}/${clubId}/${tournamentId}/${roundId}/${GAMES}`;
	const roundData = await get(ref(getDatabase(firebaseApp), LOCATION_ROUND_GAMES));

	if (roundData.exists()) {
		const decodedGames = decodeGames(roundData.val());
		return {completedGames: decodedGames.completedGames, totalGames: decodedGames.totalGames};
	} else {
		return null;
	}
}

export async function getTournaments(clubId) {
	const LOCATION_TOURNAMENTS = `${TOURNAMENTS}/${clubId}`;
	const tournamentsData = await get(ref(getDatabase(firebaseApp), LOCATION_TOURNAMENTS));

	if (tournamentsData.exists()) {
		return tournamentsData.val();
	} else {
		return null;
	}
}

export async function getClubProfilePicture(clubId) {
	const LOCATION_CLUB_PROFILE_PICTURE = `${CLUBS}/${clubId}/picture`;
	const profilePictureData = await get(ref(getDatabase(firebaseApp), LOCATION_CLUB_PROFILE_PICTURE));

	if (profilePictureData.exists()) {
		return profilePictureData.val();
	} else {
		return null;
	}
}
