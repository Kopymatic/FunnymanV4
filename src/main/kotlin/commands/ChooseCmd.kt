package commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand

class ChooseCmd : KopyCommand() {
    init {
        name = "Choose"
        description = "Choose between two or more options"
        arguments = "[choice1]/[choice2]/[choice3] ..."
        aliases = arrayOf("pick")
    }

    override suspend fun execute(event: MessageReceivedEvent) {
        val choices = removePrefix(event).split("/")
        val final: String = if (choices.size == 1) "Not enough choices provided!" else choices.random()
        reply(event, "I choose `${final.trim()}`").queue()
    }
}
