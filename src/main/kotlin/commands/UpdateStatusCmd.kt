package commands

import Reference
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.CommandError
import utilities.HybridCommand
import utilities.TextCommandData

class UpdateStatusCmd : HybridCommand() {
    override val supportsSlash = false
    override val supportsText = true
    override val slashCommandData: SlashCommandData
        get() = throw Error("This command does not support slash commands")
    override val textCommandData = TextCommandData("UpdateStatus", "updates the bots status", listOf("us", "status"))

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        throw CommandError("This command does not support slash commands")
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        if(Reference.owners.contains(event.author.id)) {
            val args = event.message.getArgs().joinToString(" ")
            try {
                event.jda.presence.activity = when {
                    args == "" -> null
                    args.startsWith("Playing", true) -> Activity.playing(args.replace("Playing", "", true))
                    args.startsWith("Competing", true) -> Activity.competing(args.replace("Competing", "", true))
                    args.startsWith("Listening", true) -> Activity.listening(args.replace("Listening", "", true))
                    args.startsWith("Watching", true) -> Activity.watching(args.replace("Watching", "", true))
                    args.startsWith("Default", true) -> Activity.watching("Version ${Reference.version} ${if (Reference.experimental) "Experimental" else ""}")
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