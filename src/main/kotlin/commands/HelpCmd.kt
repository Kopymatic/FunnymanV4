package commands

import R
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand
import utilities.kReply


class HelpCmd : KopyCommand() {
    init {
        name = "Help"
        description = "Get help with commands"
        arguments = "[command to get help with]"
    }

    override suspend fun execute(event: MessageReceivedEvent) {
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
            if (it.textCommandData.hidden) {
                null
            } else {
                Field(
                    "${R.prefixes[0]}${it.textCommandData.name} ${if (it.textCommandData.usage == null) "" else it.textCommandData.usage}",
                    it.textCommandData.description,
                    false
                )
            }
        }
        return Embed(
            color = R.defaultColor,
            title = "Text Commands",
            description = "Use `${R.prefixes[0]}help [command]` to get help with a command",
            fields = fields.filterNotNull()

        )
    }

    private fun getHelp(command: String): MessageEmbed {
        val commands = AllCommands.commands.filter { it.supportsText && it.textCommandData.name.startsWith(command) }
        val fields = commands.map {
            Field(
                "${R.prefixes[0]}${it.textCommandData.name}",
                "Description: ${it.textCommandData.description}\n" +
                        (if (it.textCommandData.usage != null) "Usage: ${it.textCommandData.usage}\n" else "") +
                        "Aliases: ${if (it.textCommandData.aliases != null) it.textCommandData.aliases!!.joinToString(", ") else "None"}\n" +
                        "Supports Slash: ${it.supportsSlash}\nSupports Text: ${it.supportsText}",
                false
            )
        }
        return Embed(
            color = R.defaultColor,
            title = (if (fields.isEmpty()) "No commands found" else "Results for $command"),
            fields = fields
        )
    }
}