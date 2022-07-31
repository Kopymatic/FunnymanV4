package commands

import dev.minn.jda.ktx.interactions.components.getOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.HybridCommand
import utilities.TextCommandData

class ChooseCmd : HybridCommand() {
    private val name = "choose"
    private val description = "Choose between two or more options"

    override val supportsSlash: Boolean = true
    override val supportsText: Boolean =  true

    override val slashCommandData: SlashCommandData = slash(name, description)
        .addOption(OptionType.STRING, "choices", "Choices to choose from, separated by commas", true, false)
    override val textCommandData: TextCommandData = TextCommandData(name, description, usage = "[choice1], [choice2], ...", aliases = listOf("pick"))

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        val choices = event.getOption<String>("choices")?.split(",")
        val final: String = choices?.random() ?: "No choices provided!"
        reply(event, "I choose `${final.trim()}`").queue()
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        val choices = removePrefix(event).split(",")
        val final: String = if (choices.size == 1) "Not enough choices provided!" else choices.random()
        reply(event, "I choose `${final.trim()}`").queue()
    }
}
