package utilities

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EverythingListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        if (event.isFromGuild) {
            if (event.message.contentRaw.contains("sex", true)) {
                val channel = event.guild.textChannels.find { channel -> channel.name == "sex-alarm" } ?: return
                channel.sendMessage("${event.member?.getNickOrUsername()} has sexed in ${event.channel.asMention} \uD83D\uDE33")
                    .queue()
            }
        }
    }
}