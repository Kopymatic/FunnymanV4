package commands

import R
import dev.minn.jda.ktx.interactions.components.getOption
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.HybridCommand
import utilities.TextCommandData
import utilities.kReply

class ChatDeadCmd : HybridCommand() {
    private val name = "chatdead"
    private val description = "Make the bot ask a question to revive chat"

    override val supportsSlash = true
    override val supportsText = true

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.STRING, "type", "The type of the question", false, true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, usage = "[type (optional)]")

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        val type = event.getOption<String>("type") ?: listOf("general question", "would you rather").random()
        event.kReply(chatDead(type)).queue()
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
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

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == "type") {
            event.replyChoiceStrings("general question", "would you rather").queue()
        }
    }
}