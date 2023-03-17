package commands

import R
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand
import utilities.kReply

class ChatDeadCmd : KopyCommand() {
    init {
        name = "ChatDead"
        description = "Make the bot ask a question to revive chat"
        arguments = "[type (optional)]"
    }

    override suspend fun execute(event: MessageReceivedEvent) {
        val type = event.message.getArgs()[0].lowercase().ifBlank {
            listOf("general question", "would you rather").random()
        }
        event.kReply(chatDead(type)).queue()
    }

    private fun chatDead(type: String): String {
        if (type != "general question" && type != "would you rather") {
            return "$type is an invalid type! Must be either \"general question\" or \"would you rather\""
        }

        //Variable to translate types to a string usable in the json
        val jsonType = if (type == "general question") "GeneralQuestions" else "WouldYouRather"

        val json = R.lists.getJSONObject("ChatDead")
        return json.getJSONArray(jsonType).toList().random().toString()
    }
}