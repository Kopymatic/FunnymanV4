package commands

import R
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.CommandException
import utilities.HybridCommand
import utilities.TextCommandData
import utilities.kReply

class SlashCommandUtilsCmd : HybridCommand() {
    override val name: String = "slashutils"
    override val description: String = "owner only."

    override val supportsSlash: Boolean = false
    override val supportsText: Boolean = true
    override val slashCommandData: SlashCommandData
        get() = throw CommandException("This command does not support slash!")
    override val textCommandData: TextCommandData = TextCommandData(name, description, hidden = true)

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        throw CommandException("This command does not support slash commands")
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        if (!R.owners.contains(event.author.id)) {
            return
        }
        val args = event.message.getArgs()
        if (args[0] == "resendall" && !R.experimental) {
            event.kReply("Beginning deletion... this may take a while.").queue()
            val commands = R.jda.retrieveCommands().complete()
            commands.forEach {
                R.log.info("Deleting command ${it.name}")
                it.delete().complete()
            }

            event.kReply("Resending commands...").queue()
            for (command in AllCommands.commands) {
                if (command.supportsSlash) {
                    R.log.info("Registering command ${command.slashCommandData.name}...")
                    R.jda.upsertCommand(command.slashCommandData).queue()
                }
            }
        } else if (args[0] == "deleteall") {
            event.kReply("Beginning deletion... this may take a while.").queue()
            val commands = R.jda.retrieveCommands().complete()
            commands.forEach {
                R.log.info("Deleting command ${it.name}")
                it.delete().complete()
            }
            event.kReply("Deleted.").queue()
        } else {
            event.kReply("unknown command").queue()
        }
    }
}