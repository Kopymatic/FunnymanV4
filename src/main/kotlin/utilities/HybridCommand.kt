package utilities

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction

abstract class HybridCommand {
    abstract val supportsSlash: Boolean
    abstract val supportsText: Boolean

    abstract val slashCommandData: SlashCommandData
    abstract val textCommandData: TextCommandData

    abstract suspend fun slashCommandReceived(event : SlashCommandInteractionEvent)

    abstract suspend fun textCommandReceived(event: MessageReceivedEvent)

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        return
    }

    open fun reply(event: SlashCommandInteractionEvent, message: String): WebhookMessageCreateAction<Message> {
        return event.kReply(message)
    }

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