package utilities

import R
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import org.json.JSONObject

fun JSONObject.getJSONList(key: String): List<Any> {
    return this.getJSONArray(key).toList()
}

fun SlashCommandInteractionEvent.kReply(message: String = R.zeroWidthSpace): WebhookMessageCreateAction<Message> {
    return this.hook.sendMessage(message)
}

fun ButtonInteractionEvent.kReply(message: String = R.zeroWidthSpace): WebhookMessageCreateAction<Message> {
    return this.hook.sendMessage(message)
}

fun MessageReceivedEvent.kReply(message: String = R.zeroWidthSpace): MessageCreateAction {
    return this.channel.sendMessage(message).setMessageReference(this.message).mentionRepliedUser(false)
}

fun SlashCommandInteractionEvent.isInDm() : Boolean {
    return this.channel.type == ChannelType.PRIVATE
}

fun MessageReceivedEvent.isInDm() : Boolean {
    return this.channel.type == ChannelType.PRIVATE
}

fun Member.getNickOrUsername() : String {
    return this.nickname ?: this.user.name
}