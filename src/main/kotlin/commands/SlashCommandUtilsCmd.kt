package commands

import R
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand
import utilities.kReply

class SlashCommandUtilsCmd : KopyCommand() {
    init {
        name = "slashutils"
        description = "Owner only."
        hidden = true
        ownerCommand = true
    }

    override suspend fun execute(event: MessageReceivedEvent) {
        if (!R.owners.contains(event.author.id)) {
            return
        }
        val args = event.message.getArgs()
        if (args[0] == "deleteall") {
            event.kReply("Beginning deletion... this may take a while.").queue()
            val commands = R.jda.retrieveCommands().complete()
            commands.forEach {
                R.log.info("Deleting command ${it.name}")
                it.delete().complete()
            }
            event.kReply("Deleted.").queue()
        } else if (args[0] == "deleteallguild") {
            event.kReply("Beginning deletion... this may take a while.").queue()
            R.jda.guilds.forEach {
                val commands = it.retrieveCommands().complete()
                commands.forEach { command ->
                    R.log.info("Deleting command ${command.name}")
                    command.delete().complete()
                }
            }
            event.kReply("Deleted.").queue()
        } else {
            event.kReply("unknown command").queue()
        }
    }
}