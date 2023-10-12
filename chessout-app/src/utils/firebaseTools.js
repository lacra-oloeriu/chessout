import {get, ref, getDatabase} from "firebase/database";
import { getStorage, ref as storageRef, getDownloadURL } from "firebase/storage";
import {firebaseApp} from "config/firebase";
const storage = getStorage(firebaseApp);

export async function readMyDefaultClub(user) {
	const USER_SETTINGS = 'userSettings';
	const USER_KEY = '$userKey';
	const DEFAULT_CLUB = 'defaultClub';
	const LOCATION_DEFAULT_CLUB = `${USER_SETTINGS}/${USER_KEY}/${DEFAULT_CLUB}`;
	const locationDefaultClub = LOCATION_DEFAULT_CLUB.replace(USER_KEY, user.uid);

	return await get(ref(getDatabase(firebaseApp), locationDefaultClub));
}

export async function readMyClubs(user) {
	const USER_SETTINGS = 'userSettings';
	const USER_KEY = '$userKey';
	const MY_CLUBS = 'myClubs';
	const DEFAULT_CLUB = 'defaultClub';
	const LOCATION_DEFAULT_CLUB = `${USER_SETTINGS}/${USER_KEY}/${DEFAULT_CLUB}`;

	const locationDefaultClub = LOCATION_DEFAULT_CLUB.replace(USER_KEY, user.uid);
	const defaultClub = await get(ref(getDatabase(firebaseApp), locationDefaultClub));

	const LOCATION_MY_CLUBS = `${USER_SETTINGS}/${USER_KEY}/${MY_CLUBS}`;
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