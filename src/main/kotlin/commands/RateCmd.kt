package commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.KopyCommand
import java.util.*

class RateCmd : KopyCommand() {
    init {
        name = "Rate"
        aliases = arrayOf("r8")
        help = "Rate something you choose"
        arguments = "[Thing to rate (Optional)]"
        guildOnly = false
    }

    override suspend fun execute(event: MessageReceivedEvent) {
        val args = event.message.getArgs().joinToString(" ")
        if (args.isEmpty()) {
            reply(event, "I'll ${verb[Random().nextInt(verb.size)]} ${event.member?.asMention} ${getRating()}").queue()
        } else {
            reply(event, "I'll ${verb[Random().nextInt(verb.size)]} \"$args\" ${getRating()}").queue()
        }
    }

    private fun getRating(): String {
        return if (Random().nextInt(ratings.size + (ratings.size / 2)) > ratings.size) { //Give random ratings a good distribution
            val secondNum = if (Random().nextInt(100) <= 10) Random().nextInt(250) else 10
            val firstNum =
                if (Random().nextInt(100) <= 10) -1 * Random().nextInt(secondNum + 10) else Random().nextInt(secondNum + 10)
            "$firstNum/$secondNum"
        } else {
            ratings[Random().nextInt(ratings.size)]
        }
    }

    private val ratings = arrayOf(
        "good/10",
        "o/k",
        "0/∞",
        "9000/1",
        "4/20",
        "7.8/10",
        "ayy/lmao",
        "swag/10",
        "they/them",
        "pog/champ",
        "poggers/10",
        "unpoggers :pensive:",
        "gamer/20",
        "nice/69",
        "un-nice/69",
        "beautiful/10",
        "cute/25",
        "hot/420",
        "cold/2",
        "divide by zero error/0",
        "awesome/10",
        "ugly/10",
        "cool/5",
        "nice cock bro. a little on the small side, but the shape is overall pretty symmetrical, and your balls have just the right amount of hair. the council rates it 7/10.",
        "6/9",
        "oozma/kappa",
        "massive tiddies / small tiddies"
    )

    private val verb = arrayOf("give", "rate", "r8")
}