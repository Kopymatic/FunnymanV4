package commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand

class PollCmd : KopyCommand() {
    init {
        name = "poll"
        description = "Creates a poll"
        arguments = "[# of reacts (Defaults to y/n question, max 10)], [Message]"
        aliases = arrayOf("pick")
    }

    private val numberEmojis = listOf(
        "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟"
    )
    private val thumbsUp = "👍"
    private val thumbsDown = "👎"

    override suspend fun execute(event: MessageReceivedEvent) {
        try {
            val optionCount = event.message.getArgs()[0].toInt()
            for (i in 0 until optionCount) {
                event.message.addReaction(getEmoji(numberEmojis[i])).queue()
            }
        } catch (e: NumberFormatException) {
            event.message.addReaction(getEmoji(thumbsUp)).queue()
            event.message.addReaction(getEmoji(thumbsDown)).queue()
        }
    }
}