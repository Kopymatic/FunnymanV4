package commands

import R
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand

class UpdateStatusCmd : KopyCommand() {
    init {
        name = "UpdateStatus"
        description = "Updates the bot's status"
        aliases = arrayOf("us, status")
        hidden = true
    }

    override suspend fun execute(event: MessageReceivedEvent) {
        if (R.owners.contains(event.author.id)) {
            val args = event.message.getArgs().joinToString(" ")
            try {
                event.jda.presence.activity = when {
                    args == "" -> null
                    args.startsWith("Playing", true) -> Activity.playing(args.replace("Playing", "", true))
                    args.startsWith("Competing", true) -> Activity.competing(args.replace("Competing", "", true))
                    args.startsWith("Listening", true) -> Activity.listening(args.replace("Listening", "", true))
                    args.startsWith("Watching", true) -> Activity.watching(args.replace("Watching", "", true))
                    args.startsWith(
                        "Default",
                        true
                    ) -> Activity.watching("V${R.version} ${if (R.experimental) "Experimental" else ""}")
                    else -> Activity.playing(args)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                event.message.addReaction(getEmoji("❌")).queue()
                return
            }
            event.message.addReaction(getEmoji("✅")).queue()
        }
    }

}