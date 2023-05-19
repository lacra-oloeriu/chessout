package eu.chessout.shared;

import static eu.chessout.shared.Constants.CHAT_KEY;
import static eu.chessout.shared.Constants.CLUBS;
import static eu.chessout.shared.Constants.CLUB_KEY;
import static eu.chessout.shared.Constants.DISPLAY_NAME;
import static eu.chessout.shared.Constants.FIDE_ID;
import static eu.chessout.shared.Constants.LOCATION_CHAT;
import static eu.chessout.shared.Constants.LOCATION_CLUB_POST;
import static eu.chessout.shared.Constants.LOCATION_CLUB_POST_LIKE;
import static eu.chessout.shared.Constants.LOCATION_CLUB_POST_PICTURE;
import static eu.chessout.shared.Constants.LOCATION_CLUB_USERS_SETTINGS;
import static eu.chessout.shared.Constants.LOCATION_CLUB_USER_SETTINGS;
import static eu.chessout.shared.Constants.LOCATION_PLAYER_FOLLOWERS;
import static eu.chessout.shared.Constants.LOCATION_ROUND_GAMES;
import static eu.chessout.shared.Constants.LOCATION_TOURNAMENT;
import static eu.chessout.shared.Constants.LOCATION_TOURNAMENT_PLAYERS;
import static eu.chessout.shared.Constants.LOCATION_USER_DISPLAY_NAME;
import static eu.chessout.shared.Constants.LOCATION_USER_EMAIL;
import static eu.chessout.shared.Constants.LOCATION_USER_FOLLOWED_PLAYERS;
import static eu.chessout.shared.Constants.LOCATION_USER_PROFILE_PICTURE;
import static eu.chessout.shared.Constants.LOCATION_USER_PROFILE_PICTURES;
import static eu.chessout.shared.Constants.LOCATION_USER_STREAM_POST;
import static eu.chessout.shared.Constants.PICTURE_KEY;
import static eu.chessout.shared.Constants.PLAYER_KEY;
import static eu.chessout.shared.Constants.POST_KEY;
import static eu.chessout.shared.Constants.TOURNAMENT_KEY;
import static eu.chessout.shared.Constants.USER_KEY;

import eu.chessout.shared.model.ChatItem;
import eu.chessout.shared.model.Post;

public class Locations {
    public static String gameLocation(String clubId, String tournamentId, String roundId, String tableId) {
        return Constants.LOCATION_GAME
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.TOURNAMENT_KEY, tournamentId)
                .replace(Constants.ROUND_NUMBER, String.valueOf(roundId))
                .replace(Constants.TABLE_NUMBER, String.valueOf(tableId));
    }

    public static String gamesFolder(
            String clubId,
            String tournamentKey,
            int roundId
    ) {
        return Constants.LOCATION_ROUND_GAMES
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.TOURNAMENT_KEY, tournamentKey)
                .replace(Constants.ROUND_NUMBER, String.valueOf(roundId));
    }


    public static String buildDevicesLocation(String userId) {
        return Constants.LOCATION_MY_DEVICES
                .replace(Constants.USER_KEY, userId);
    }

    public static String url(String location, String accessToken) {
        return Constants.FIREBASE_URL + location
                + ".json?access_token=" + accessToken;
    }

    public static String clubPlayer(String clubId, String playerId) {
        return Constants.LOCATION_CLUB_PLAYER
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.PLAYER_KEY, playerId);
    }

    public static String clubPlayerList(String clubId) {
        return Constants.LOCATION_CLUB_PLAYERS
                .replace(Constants.CLUB_KEY, clubId);
    }

    public static String clubPlayerArchiveState(String clubId, String playerId) {
        return clubPlayer(clubId, playerId)
                + "/" + "archived";
    }

    public static String clubPictureFolder(String clubId) {
        return LOCATION_CLUB_POST_PICTURE
                .replace(CLUB_KEY, clubId)
                .replace("/" + PICTURE_KEY, "");
    }

    public static String clubPictureLocation(String clubId, String pictureId) {
        return LOCATION_CLUB_POST_PICTURE
                .replace(CLUB_KEY, clubId)
                .replace(PICTURE_KEY, pictureId);
    }

    public static String clubPictureUri(String clubId, String pictureId, String pictureName) {
        return clubPictureLocation(clubId, pictureId) + "/" + pictureName;
    }

    public static String clubPostFolder(String clubId) {
        return LOCATION_CLUB_POST
                .replace(CLUB_KEY, clubId)
                .replace("/" + POST_KEY, "");
    }

    public static String clubPostLocation(String clubId, String postId) {
        return LOCATION_CLUB_POST
                .replace(CLUB_KEY, clubId)
                .replace(POST_KEY, postId);
    }

    public static String pictureInPostLocation(Post post, int index) {
        return clubPostLocation(post.getClubId(), post.getPostId())
                + "/pictures/" + index + "/uploadComplete";

    }

    // <user public info>
    public static String userDisplayName(String userId) {
        return LOCATION_USER_DISPLAY_NAME
                .replace(USER_KEY, userId);
    }

    public static String userEmailValue(String userId) {
        return LOCATION_USER_EMAIL
                .replace(USER_KEY, userId);
    }

    public static String userProfilePicture(String userId) {
        return LOCATION_USER_PROFILE_PICTURE
                .replace(USER_KEY, userId);
    }

    public static String userProfilePictureFolder(String userId) {
        return LOCATION_USER_PROFILE_PICTURES
                .replace(USER_KEY, userId)
                .replace("/" + PICTURE_KEY, "");
    }

    public static String userProfilePictureItem(String userId, String pictureId) {
        return LOCATION_USER_PROFILE_PICTURES
                .replace(USER_KEY, userId)
                .replace(PICTURE_KEY, pictureId);
    }


    // <//user public info>

    public static String club(String clubId) {
        return CLUBS + "/" + clubId;
    }

    public static String clubProfilePicture(String clubId) {
        return CLUBS + "/" + clubId + "/picture";
    }


    public static String likeFolder(String clubId, String postId) {
        return LOCATION_CLUB_POST_LIKE
                .replace(CLUB_KEY, clubId)
                .replace(POST_KEY, postId)
                .replace("/" + USER_KEY, "");
    }

    public static String like(String userId, String clubId, String postId) {
        return LOCATION_CLUB_POST_LIKE
                .replace(CLUB_KEY, clubId)
                .replace(POST_KEY, postId)
                .replace(USER_KEY, userId);
    }

    // tournament
    public static String tournament(String clubId, String tournamentId) {
        return LOCATION_TOURNAMENT
                .replace(CLUB_KEY, clubId)
                .replace(TOURNAMENT_KEY, tournamentId);
    }


    public static String tournamentPlayers(String clubId, String tournamentId) {
        return LOCATION_TOURNAMENT_PLAYERS
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.TOURNAMENT_KEY, tournamentId);
    }


    public static String tournamentGames(String clubId, String tournamentId, String roundId) {
        return LOCATION_ROUND_GAMES
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.TOURNAMENT_KEY, tournamentId)
                .replace(Constants.ROUND_NUMBER, roundId);
    }

    public static String clubUsersSettings(String clubId) {
        return LOCATION_CLUB_USERS_SETTINGS
                .replace(CLUB_KEY, clubId);
    }

    public static String clubSettings(String clubId, String userId) {
        return LOCATION_CLUB_USER_SETTINGS
                .replace(CLUB_KEY, clubId)
                .replace(USER_KEY, userId);
    }

    public static String userStreamPostFolder(String userId) {
        return LOCATION_USER_STREAM_POST
                .replace(USER_KEY, userId)
                .replace("/" + POST_KEY, "");
    }

    public static String userStreamPost(String userId, String postId) {
        return LOCATION_USER_STREAM_POST
                .replace(USER_KEY, userId)
                .replace(POST_KEY, postId);
    }

    public static String pictureInUserStreamPostUploadComplete(String userId, String postId, int index) {
        return userStreamPost(userId, postId) + "/pictures/" + index + "/uploadComplete";
    }

    public static String chatFolder(ChatItem chat) {
        String chatFolderKey = getFolderKey(chat);
        return LOCATION_CHAT
                .replace(CHAT_KEY, chatFolderKey);

    }

    private static String getFolderKey(ChatItem chat) {
        if (chat.getLocationType() == ChatItem.LocationType.CLUB_TOURNAMENT_PAIRINGS_AVAILABLE) {
            String chatFolderKey = chat.getChatTournamentId() + "-round-" + chat.getChatRoundId();
            return chatFolderKey;
        } else if (chat.getLocationType() == ChatItem.LocationType.POST) {
            return chat.getPostId();
        }
        throw new IllegalStateException("Not supported type " + chat.getLocationType());
    }

    public static String playerFollowersFolder(String playerId) {
        String sectionToRemove = "/" + USER_KEY;
        return Constants.LOCATION_PLAYER_FOLLOWERS
                .replace(Constants.PLAYER_KEY, playerId)
                .replace(sectionToRemove, ""); // exclude individual followers
    }

    public static String followPlayerSettings(String playerId, String userId) {
        return LOCATION_PLAYER_FOLLOWERS
                .replace(PLAYER_KEY, playerId)
                .replace(USER_KEY, userId);
    }

    public static String followedPlayersFolder(String userId) {
        String sectionToRemove = "/" + PLAYER_KEY;
        return LOCATION_USER_FOLLOWED_PLAYERS
                .replace(USER_KEY, userId)
                .replace(sectionToRemove, ""); // exclude player keys
    }

    public static String followedPlayer(String userId, String playerId) {
        return LOCATION_USER_FOLLOWED_PLAYERS
                .replace(USER_KEY, userId)
                .replace(PLAYER_KEY, playerId);
    }

    public static String userPublicInfo(String userId) {
        String sectionToRemove = "/" + DISPLAY_NAME;
        return LOCATION_USER_DISPLAY_NAME
                .replace(USER_KEY, userId)
                .replace(sectionToRemove, "");// get all public info
    }

    public static String fideFollowersFolder(String fideId) {
        String sectionToRemove = "/" + USER_KEY;
        return Constants.LOCATION_FIDE_ID_FOLLOWER
                .replace(Constants.FIDE_ID, fideId)
                .replace(sectionToRemove, "");
    }

    public static String followFideIdSettings(String fideId, String userId) {
        return Constants.LOCATION_FIDE_ID_FOLLOWER
                .replace(FIDE_ID, fideId)
                .replace(USER_KEY, userId);
    }
}
