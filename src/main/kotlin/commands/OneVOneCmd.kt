package commands

import Reference
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import utilities.HybridCommand
import utilities.TextCommandData
import utilities.getJSONList
import utilities.kReply

class OneVOneCmd : HybridCommand() {
    private val name = "onevone"
    private val description = "Battle it out between two things"

    override val supportsSlash: Boolean = true
    override val supportsText: Boolean =  true

    override val slashCommandData: SlashCommandData = slash(name, description)
        .addOption(OptionType.STRING, "thing1", "The first thing in the battle", true)
        .addOption(OptionType.STRING, "thing2", "The second thing in the battle", true)
    override val textCommandData: TextCommandData = TextCommandData(name, description, aliases = listOf("1v1"), usage = "[thing1], [thing2]")

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        val thing1 = event.getOption<String>("thing1")!!
        val thing2 = event.getOption<String>("thing2")!!
        event.kReply(Reference.zeroWidthSpace).addEmbeds(oneVOne(thing1, thing2)).queue()
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        val args = event.message.getArgs()
        if (args.size < 2) {
            event.kReply("You need to provide two things to battle!").queue()
            return
        }
        val thing1 = args[0]
        val thing2 = args[1]
        event.kReply(Reference.zeroWidthSpace).setEmbeds(oneVOne(thing1, thing2)).queue()
    }

    private fun oneVOne(thing1: String, thing2: String): MessageEmbed {
        if (thing1 == thing2) {
            return Embed(
                color = Reference.red,
                description = "Two $thing1 cannot occupy the same space at the same time")
        }
        val list = listOf(thing1, thing2)
        val winner = list.random()
        val loser = list.filterNot(winner::equals).first()

        //This has the JSON object containing the OneVOne titles, actions, and descriptors
        val jsonObject = Reference.lists.getJSONObject("OneVOne")
        val title = jsonObject.getJSONList("titles").random().toString()
        val action = jsonObject.getJSONList("actions").random().toString()
        val descriptor = jsonObject.getJSONList("descriptors").random().toString()

        return Embed {
            color = Reference.defaultColor
            this.title = title
            description = "$winner **$action** $loser **$descriptor**"
        }
    }
}