package utilities

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction

abstract class KopyCommand {
    var name = "null"
    var description = "Description not set"
    var help: String? = null
    var category: String? = null
    var arguments: String? = null
    var guildOnly = true
    var requiredRole: String? = null
    var ownerCommand = false

    //TODO Implement this
    var userPermissions: Array<Permission>? = null

    //TODO implement this too
    var botPermissions: Array<Permission>? = null
    var aliases: Array<String>? = null
    var hidden = false

    suspend fun run(event: MessageReceivedEvent) {
        if (guildOnly && !event.isFromGuild) return
        //check if the event is from the guild before getting the member to prevent The Bad(TM)
        if (event.isFromGuild && requiredRole != null && event.member!!.roles.find { role -> role.name == this.requiredRole } != null) return

        //TODO Implement the permissions stuff

        execute(event)
    }

    abstract suspend fun execute(event: MessageReceivedEvent)

    open fun reply(event: MessageReceivedEvent, message: String): MessageCreateAction {
        return event.kReply(message)
    }

    open fun removePrefix(message: String): String {
        return message.split(" ").drop(1).joinToString(" ")
    }

    open fun removePrefix(messageEvent: MessageReceivedEvent): String {
        return messageEvent.message.contentRaw.split(" ").drop(1).joinToString(" ")
    }

    open fun getEmoji(emoji: String): EmojiUnion {
        return Emoji.fromFormatted(emoji)
    }

    fun Message.getArgs(): List<String> {
        return removePrefix(this.contentRaw).split(" ")
    }
}