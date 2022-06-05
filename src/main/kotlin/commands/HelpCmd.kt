package commands

import Reference
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.HybridCommand
import utilities.TextCommandData
import utilities.kReply


class HelpCmd : HybridCommand() {
    private val name = "help"
    private val description = "Get help with text commands"

    override val supportsSlash: Boolean = true
    override val supportsText: Boolean = true

    override val slashCommandData: SlashCommandData = Commands.slash(name, description)
        .addOption(OptionType.STRING, "command", "Command to get help with", false, true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, usage = "[command to get help with]")

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        val option = event.getOption<String>("command")
        if (option == null) {
            event.kReply().addEmbeds(getHelp()).queue()
        } else {
            event.kReply().addEmbeds(getHelp(option)).queue()
        }
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        val option = event.message.getArgs()[0]
        if (option.isBlank()) {
            event.kReply().setEmbeds(getHelp()).queue()
        } else {
            event.kReply().setEmbeds(getHelp(option)).queue()
        }
    }

    private fun getHelp(): MessageEmbed {
        val commands = AllCommands.commands.filter { it.supportsText }
        val fields = commands.map {
            Field(
                "${Reference.prefixes[0]}${it.textCommandData.name} ${if (it.textCommandData.usage == null) "" else it.textCommandData.usage}",
                it.textCommandData.description,
                false
            )
        }
        return Embed(
            color = Reference.defaultColor,
            title = "Text Commands",
            description = "Use `${Reference.prefixes[0]}help [command]` to get help with a command",
            fields = fields

        )
    }

    private fun getHelp(command: String): MessageEmbed {
        val commands = AllCommands.commands.filter { it.supportsText && it.textCommandData.name.startsWith(command) }
        val fields = commands.map {
            Field(
                "${Reference.prefixes[0]}${it.textCommandData.name}",
                "Description: ${it.textCommandData.description}\n" +
                        (if (it.textCommandData.usage != null) "Usage: ${it.textCommandData.usage}\n" else "") +
                        "Aliases: ${if(it.textCommandData.aliases != null) it.textCommandData.aliases!!.joinToString(", ") else "None"}\n" +
                        "Supports Slash: ${it.supportsSlash}\nSupports Text: ${it.supportsText}",
                false
            )
        }
        return Embed(
            color = Reference.defaultColor,
            title = (if (fields.isEmpty()) "No commands found" else "Results for $command"),
            fields = fields
        )
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == "command") {
            val options = AllCommands.commands.map { it.textCommandData.name }
                .filter {
                    event.focusedOption.value.isBlank() || it.lowercase()
                        .startsWith(event.focusedOption.value.lowercase())
                }
                .map { Command.Choice(it, it) }
            event.replyChoices(options).queue()
        }
    }
}