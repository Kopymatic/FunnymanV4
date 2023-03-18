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
        val commands = AllCommands.commands
        val fields = commands.map {
            if (it.hidden) {
                null
            } else {
                Field(
                    "${R.prefixes[0]}${it.name} ${if (it.arguments == null) "" else it.arguments}",
                    it.description,
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
        val commands = AllCommands.commands.filter { it.name.startsWith(command) }
        val fields = commands.map {
            Field(
                "${R.prefixes[0]}${it.name}",
                "Description: ${it.description}\n" +
                        (if (it.arguments != null) "Usage: ${it.arguments}\n" else "") +
                        "Aliases: ${if (it.aliases != null) it.aliases!!.joinToString(", ") else "None"}\n",
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