package commands

import R
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import utilities.HybridCommand
import utilities.TextCommandData
import utilities.kReply

class PingCmd : HybridCommand() {
    private val name = "ping"
    private val description = "Ping pong!"

    override val supportsSlash: Boolean = true
    override val supportsText: Boolean = true

    override val slashCommandData: SlashCommandData = slash(name, description)
    override val textCommandData: TextCommandData = TextCommandData(name, description)

    private val pingButton =
        R.jda.button(label = "Pong!", style = ButtonStyle.SUCCESS) { button ->
            button.reply_("Pong 2!").setEphemeral(true).queue()
        }

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        event.kReply("Pong!").setComponents(ActionRow.of(pingButton)).queue()
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        event.kReply("Pong!").setComponents(ActionRow.of(pingButton)).queue()
    }
}