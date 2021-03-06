package commands

import utilities.HybridCommand
import Reference
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import utilities.TextCommandData

class PingCmd : HybridCommand() {
    override val supportsSlash: Boolean = true
    override val supportsText: Boolean = true

    override val slashCommandData: SlashCommandData = slash("ping", "Ping pong!")
    override val textCommandData: TextCommandData = TextCommandData("ping", "Ping pong!")

    private val pingButton =
        Reference.jda.button(label = "Pong!", style = ButtonStyle.SUCCESS) { button ->
            button.reply_("Pong 2!").setEphemeral(true).queue()
        }

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        event.hook.sendMessage("Pong!").addActionRow(pingButton).queue()
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        event.message.reply_("Pong!").setActionRows(ActionRow.of(pingButton)).queue()
    }

}