package eu.chessout.v2.util

import com.fasterxml.jackson.databind.ObjectMapper
import eu.chessdata.chesspairing.model.ChesspairingTournament

class MyJsonUtil {
    companion object {
        fun tournamentToJson(chesspairingTournament: ChesspairingTournament): String {
            val objectMapper = ObjectMapper()
            return objectMapper.writeValueAsString(chesspairingTournament)
        }
    }
}