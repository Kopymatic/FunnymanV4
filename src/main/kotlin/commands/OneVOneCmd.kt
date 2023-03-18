package commands

import R
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand
import utilities.getJSONList
import utilities.kReply

class OneVOneCmd : KopyCommand() {
    init {
        name = "OneVOne"
        description = "Battle it out between two things"
        arguments = "[thing1] [thing2]"
        aliases = arrayOf("1v1")
    }

    override suspend fun execute(event: MessageReceivedEvent) {
        val args = event.message.getArgs()
        if (args.size < 2) {
            event.kReply("You need to provide two things to battle!").queue()
            return
        }
        val thing1 = args[0]
        val thing2 = args[1]
        event.kReply(R.zeroWidthSpace).setEmbeds(oneVOne(thing1, thing2)).queue()
    }

    private fun oneVOne(thing1: String, thing2: String): MessageEmbed {
        if (thing1 == thing2) {
            return Embed(
                color = R.red,
                description = "Two $thing1 cannot occupy the same space at the same time"
            )
        }
        val list = listOf(thing1, thing2)
        val winner = list.random()
        val loser = list.filterNot(winner::equals).first()

        //This has the JSON object containing the OneVOne titles, actions, and descriptors
        val jsonObject = R.lists.getJSONObject("OneVOne")
        val title = jsonObject.getJSONList("titles").random().toString()
        val action = jsonObject.getJSONList("actions").random().toString()
        val descriptor = jsonObject.getJSONList("descriptors").random().toString()

        return Embed {
            color = R.defaultColor
            this.title = title
            description = "$winner **$action** $loser **$descriptor**"
        }
    }
}