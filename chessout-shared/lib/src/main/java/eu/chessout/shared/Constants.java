package eu.chessout.shared;


public class Constants {


    /**
     * System
     */
    public static final String LOG_TAG = "my-debug";
    public static final String FIREBASE_URL = "https://chess-out-v2.firebaseio.com/";

    /*
     * production: "https://chess-out-v2.appspot.com/"
     * localhost: "http://192.168.1.37:8080/"
     */
    public static final String API_URL = "https://chess-out-v2.appspot.com/";
    public static final Boolean IS_IN_DEBUG_MODE = false;


    /**
     * LOCATIONS
     */
    public static final String USERS = "users";
    public static final String USER_KEY = "$userKey";
    public static final String CLUBS = "clubs";
    public static final String CLUB_KEY = "$clubKey";
    public static final String CLUB_NAME = "clubName";
    public static final String CLUB_MANAGERS = "clubManagers";
    public static final String MANAGER_KEY = "$managerKey";
    public static final String USER_SETTINGS = "userSettings";

    public static final String MY_CLUBS = "myClubs";
    public static final String MY_CLUB = "myClub";
    public static final String MY_DEVICES = "myDevices";
    public static final String DEVICE_KEY = "_deviceKey";

    public static final String DEFAULT_CLUB = "defaultClub";
    public static final String TOURNAMENTS = "tournaments";
    public static final String TOURNAMENT_KEY = "$tournamentKey";

    public static final String CLUB_PLAYERS = "clubPlayers";
    public static final String PLAYER_KEY = "$playerKey";

    public static final String TOURNAMENT_PLAYERS = "tournamentPlayers";

    public static final String TOURNAMENT_ROUNDS = "tournamentRounds";
    public static final String STANDINGS = "standings";
    public static final String TOURNAMENT_INITIAL_ORDER = "tournamentInitialOrder";
    public static final String STANDING_NUMBER = "$standingNumber";
    public static final String TOTAL_ROUNDS = "totalRounds";
    public static final String ROUND_NUMBER = "$roundNumber";
    //public static final String ROUND_PLAYERS = "roundPlayers";
    public static final String ROUND_ABSENT_PLAYERS = "absentPlayers";
    public static final String DATA_PLACEHOLDER = "dataPlaceHolder";
    public static final String TABLE_NUMBER = "$tableNumber";
    public static final String GAMES = "games";
    public static final String RESULT = "result";
    public static final String WHITE_PLAYER_NAME = "whitePlayerName";
    public static final String BLACK_PLAYER_NAME = "blackPlayerName";
    public static final String NO_PARTNER = "noPartner";
    public static final String CURRENT_RESULT = "currentResult";
    public static final String GLOBAL_FOLLOWERS = "globalFollowers";
    public static final String BY_PLAYER = "byPlayer";
    public static final String FOLLOWED_PLAYERS = "followedPlayers";
    public static final String MEDIA = "media";
    public static final String PLAYER = "player";
    public static final String CLUB = "club";
    public static final String PROFILE_PICTURE = "profilePicture";

    public static final String PLAYER_FOLLOWERS = "playerFollowers";
    public static final String FIDE_ID_FOLLOWERS = "fideIdFollowers";
    public static final String FIDE_ID = "$fideId";


    //clubManagers/$clubKey/$managerKey
    public static final String LOCATION_CLUB_MANAGERS = CLUB_MANAGERS + "/" + CLUB_KEY + "/" + MANAGER_KEY;

    //userSettings/$userKey/myClubs
    //public static final String LOCATION_MY_CLUBS = USER_SETTINGS + "/" + USER_KEY + "/" + MY_CLUBS;
    //userSettings/$userKey/myClubs
    public static final String LOCATION_MY_CLUBS = USER_SETTINGS + "/" + USER_KEY + "/" + MY_CLUBS + "/" + CLUB_KEY;

    //userSettings/$userKey/myClub
    public static final String LOCATION_L_MY_CLUB = USER_SETTINGS + "/" + USER_KEY + "/" + MY_CLUB;

    //userSettings/$userKey/defaultClub
    public static final String LOCATION_DEFAULT_CLUB = USER_SETTINGS + "/" + USER_KEY + "/" + DEFAULT_CLUB;

    //userSettings/$userKey/myDevices
    public static final String LOCATION_MY_DEVICES = USER_SETTINGS + "/" + USER_KEY + "/" + MY_DEVICES;
    //userSettings/$userKey/myDevices/$deviceKey
    public static final String LOCATION_MY_DEVICE = LOCATION_MY_DEVICES + "/" + DEVICE_KEY;


    //tournaments/$clubKey
    public static final String LOCATION_TOURNAMENTS = TOURNAMENTS + "/" + CLUB_KEY;

    //tournaments/$clubKey/$tournamentKey
    public static final String LOCATION_TOURNAMENT = LOCATION_TOURNAMENTS + "/" + TOURNAMENT_KEY;

    //clubPlayers/$clubKey
    public static final String LOCATION_CLUB_PLAYERS = CLUB_PLAYERS + "/" + CLUB_KEY;

    //clubPlayers/$clubKey/$playerKey
    public static final String LOCATION_CLUB_PLAYER = LOCATION_CLUB_PLAYERS + "/" + PLAYER_KEY;

    //tournamentPlayers/$clubKey/$tournamentKey/
    public static final String LOCATION_TOURNAMENT_PLAYERS = TOURNAMENT_PLAYERS + "/"
            + CLUB_KEY + '/'
            + TOURNAMENT_KEY;

    //tournamentRounds/$clubKey/$tournamentKey/$roundNumber/games
    public static final String LOCATION_ROUND_GAMES = TOURNAMENT_ROUNDS + "/"
            + CLUB_KEY + '/'
            + TOURNAMENT_KEY + "/" + ROUND_NUMBER + "/" + GAMES;
    //tournamentRounds/$clubKey/$tournamentKey/$roundNumber/games/$tableNumber
    public static final String LOCATION_GAME = LOCATION_ROUND_GAMES + "/" + TABLE_NUMBER;

    //tournamentRounds/$tournamentKey/$roundNumber/games/$tableNumber/result
    public static final String LOCATION_GAME_RESULT = LOCATION_ROUND_GAMES +
            "/" + TABLE_NUMBER + "/" + RESULT;

    //tournamentRounds/$tournamentKey/$roundNumber/roundPlayers
    //public static final String LOCATION_ROUND_PLAYERS = TOURNAMENT_ROUNDS + "/" + TOURNAMENT_KEY + "/" + ROUND_NUMBER + "/" + ROUND_PLAYERS;
    //tournamentRounds/$tournamentKey/$roundNumber/absentPlayers
    public static final String LOCATION_ROUND_ABSENT_PLAYERS = TOURNAMENT_ROUNDS + "/" +
            CLUB_KEY + "/" +
            TOURNAMENT_KEY + "/" + ROUND_NUMBER + "/" + ROUND_ABSENT_PLAYERS;


    public static final String CATEGORY_NAME = "$categoryName";
    public static final String CATEGORY_DEFAULT = "defaultCategory";

    //tournamentRounds/$tournamentKey/$roundNumber/standings/$categoryName/$standingNumber
    public static final String LOCATION_STANDINGS = TOURNAMENT_ROUNDS + "/" +
            CLUB_KEY + "/" +
            TOURNAMENT_KEY + "/" + ROUND_NUMBER + "/" + STANDINGS + "/" + CATEGORY_NAME + "/" + STANDING_NUMBER;

    //tournamentInitialOrder/$tournamentKey/$playerKey
    public static final String LOCATION_TOURNAMENT_PLAYER_INITIAL_ORDER = TOURNAMENT_INITIAL_ORDER + "/"
            + CLUB_KEY + "/"
            + TOURNAMENT_KEY + "/"
            + PLAYER_KEY;

    //tournamentInitialOrder/$tournamentKey
    public static final String LOCATION_TOURNAMENT_INITIAL_ORDER = TOURNAMENT_INITIAL_ORDER + "/"
            + CLUB_KEY + "/"
            + TOURNAMENT_KEY;


    //globalFollowers/byPlayer/$playerKey
    //public static final String LOCATION_GLOBAL_FOLLOWERS_BY_PLAYER = GLOBAL_FOLLOWERS + "/" + BY_PLAYER + "/" + PLAYER_KEY;
    //globalFollowers/byPlayer/$playerKey/$userKey
    //public static final String LOCATION_GLOBAL_FOLLOWER_BY_PLAYER = LOCATION_GLOBAL_FOLLOWERS_BY_PLAYER + "/" + USER_KEY;


    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";


    public static final String IS_ADMIN = "isAdmin";

    //crowd tournaments section
    public static final String LOCATION_CROWD_TOURNAMENTS = "crowd-tournaments";

    public static final String LOCATION_USER_SETTINGS_CROWD_TOURNAMENTS = USER_SETTINGS + "/" + USER_KEY + "/" + LOCATION_CROWD_TOURNAMENTS + "/" + TOURNAMENT_KEY;

    //"crowd-who-follows-tournament/TOURNAMENT_KEY/USER_KEY
    public static final String LOCATION_CROWD_WHO_FOLLOWS_TOURNAMENT = "crowd-who-follows-tournament" + "/" + TOURNAMENT_KEY;

    //crowd-tournaments/$tournamentKey
    //public static final String LOCATION_CROWD_TOURNAMENT = LOCATION_CROWD_TOURNAMENTS +"/"+TOURNAMENT_KEY;

    // media/club/$clubKey/player/$playerKey
    public static final String LOCATION_PLAYER_MEDIA = MEDIA + "/" + CLUB + "/"
            + CLUB_KEY + "/" + PLAYER + "/" + PLAYER_KEY;

    // media/club/$clubKey/player/$playerKey/profilePicture
    public static final String LOCATION_PLAYER_MEDIA_PROFILE_PICTURE = LOCATION_PLAYER_MEDIA +
            "/" + PROFILE_PICTURE;

    /**
     * Posts section
     * <p>
     * clubPosts/$clubKey/postItems/$postKey/{postObject type: clubPost}
     * - only club admins should be allowed to create items
     * <p>
     * clubPosts/$clubKey/postComments/$postKey/$commentKey/$userKey/{postObject type: comment}
     * - only create and edit comments generated by the same user
     * <p>
     * clubPosts/$clubKey/postLikes/$postKey/$userKey/{postObject type: Comment}
     * - only add and remove likes generated by the same user
     */

    public static final String CLUB_POSTS = "clubPosts";
    public static final String CLUB_POST_PICTURES = "clubPostPictures";
    public static final String PICTURE_KEY = "$pictureKey";
    public static final String POST_ITEMS = "postItems";
    public static final String POST_KEY = "$postKey";
    public static final String POST_LIKES = "postLikes";

    public static final String LOCATION_CLUB_POST = CLUB_POSTS + "/" +
            CLUB_KEY + "/" + POST_ITEMS + "/" +
            POST_KEY;
    public static final String LOCATION_CLUB_POST_PICTURE = CLUB_POST_PICTURES + "/" +
            CLUB_KEY + "/" +
            PICTURE_KEY;
    public static final String LOCATION_CLUB_POST_LIKE = CLUB_POSTS + "/" +
            CLUB_KEY + "/" + POST_LIKES + "/" +
            POST_KEY + "/" +
            USER_KEY;

    /**
     * userPublicInfo
     * - all this comment section refers to public to read items
     * - only the respective user can update its own public info
     * <p>
     * displayName (location)
     * userPublicInfo/$userKey/displayName
     * <p>
     * profilePhoto (location)
     * userPublicInfo/$userKey/profilePicture
     * <p>
     * profilePictures (album location)
     * userPublicInfo/$userKey/media/profilePictures/$pictureKey
     */

    public static final String USER_PUBLIC_INFO = "userPublicInfo";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PROFILE_PICTURES = "profilePictures";
    public static final String EMAIL = "email";
    public static final String LOCATION_USER_DISPLAY_NAME = USER_PUBLIC_INFO + "/" +
            USER_KEY + "/" + DISPLAY_NAME;
    public static final String LOCATION_USER_PROFILE_PICTURE = USER_PUBLIC_INFO + "/" +
            USER_KEY + "/" + PROFILE_PICTURE;
    public static final String LOCATION_USER_PROFILE_PICTURES = USER_PUBLIC_INFO + "/" +
            USER_KEY + "/" + MEDIA + "/" + PROFILE_PICTURES + "/" +
            PICTURE_KEY;
    public static final String LOCATION_USER_EMAIL = USERS + "/" + USER_KEY + "/" + EMAIL;

    /**
     * The user can manage his own follow state
     */
    public static final String CLUB_USER_SETTINGS = "clubUsersSettings";
    public static final String LOCATION_CLUB_USERS_SETTINGS = CLUB_USER_SETTINGS + "/" +
            CLUB_KEY;
    public static final String LOCATION_CLUB_USER_SETTINGS = LOCATION_CLUB_USERS_SETTINGS + "/" +
            USER_KEY;

    /**
     *
     */
    public static final String USER_STREAM = "userStream";
    public static final String LOCATION_USER_STREAM_POST = USER_SETTINGS + "/"
            + USER_KEY + "/" + USER_STREAM + "/" + POST_ITEMS + "/"
            + POST_KEY;

    /**
     * Chat
     */
    public static final String CHAT = "chat";
    public static final String CHAT_ITEMS = "chatItems";
    public static final String CHAT_KEY = "$chatKey";
    public static final String LOCATION_CHAT = CHAT + "/"
            + CHAT_KEY + "/" + CHAT_ITEMS;

    /**
     * FOLLOW PLAYERS
     */

    /**
     * it contains the list of players followed by user
     */
    //userSettings/$userKey/followedPlayers/$playerKey
    public static final String LOCATION_USER_FOLLOWED_PLAYERS = USER_SETTINGS + "/" +
            USER_KEY + "/" + FOLLOWED_PLAYERS + "/" +
            PLAYER_KEY;

    /**
     * It contains the list of users that follow a player as follow player settings
     * Each player can manage its own settings as FollowPlayerSettings
     */
    public static final String LOCATION_PLAYER_FOLLOWERS = PLAYER_FOLLOWERS + "/" +
            PLAYER_KEY + "/" +
            USER_KEY;

    /**
     * Location that contains the list of users that follow a specific fide id.
     * Each player can manage its own settings as Follow player id
     */
    public static final String LOCATION_FIDE_ID_FOLLOWER = FIDE_ID_FOLLOWERS + "/" +
            FIDE_ID + "/" +
            USER_KEY;
}
