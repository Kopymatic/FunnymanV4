package commands

import dev.minn.jda.ktx.interactions.components.getOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.CommandError
import utilities.HybridCommand
import utilities.TextCommandData

class PollCmd : HybridCommand() {
    private val name = "poll"
    private val description = "Creates a poll"

    private val numberEmojis = listOf(
        "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟"
    )
    private val thumbsUp = "👍"
    private val thumbsDown = "👎"

    override val supportsSlash: Boolean = true
    override val supportsText: Boolean = true

    override val slashCommandData: SlashCommandData = Commands.slash(name, description)
        .addOption(OptionType.STRING, "text", "The text to be displayed with the poll", true)
        .addOption(OptionType.INTEGER, "options", "How many options there are for your poll", false)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, usage = "[# of reacts (Default 2, max 10)], [Message]")

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        val text = event.getOption<String>("text") ?: throw CommandError("text is a required option for Poll!")
        val optionCount = event.getOption<Int>("options")
        reply(event, text).queue {
            if(optionCount != null) {
                for(i in 0 until optionCount) {
                    it.addReaction(getEmoji(numberEmojis[i])).queue()
                }
            } else {
                it.addReaction(getEmoji(thumbsUp)).queue()
                it.addReaction(getEmoji(thumbsDown)).queue()
            }
        }
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        try {
            val optionCount = event.message.getArgs()[0].toInt()
            for(i in 0 until optionCount) {
                event.message.addReaction(getEmoji(numberEmojis[i])).queue()
            }
        } catch (e: NumberFormatException) {
            event.message.addReaction(getEmoji(thumbsUp)).queue()
            event.message.addReaction(getEmoji(thumbsDown)).queue()
        }
    }
}