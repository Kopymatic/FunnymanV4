package commands

import R
import database.LoveCommands
import dev.minn.jda.ktx.events.awaitButton
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utilities.*
import kotlin.random.Random

abstract class LoveCmds : KopyCommand() {
    open val usage: String = "[user as @mention]"

    /**
     * Array of gifs the command can pick from
     */
    abstract val gifs: List<String>

    /**
     * The ActionIdentifier is 4 characters that will be used to identify this command in the database
     */
    abstract val actionIdentifier: String

    /**
     * The title text used in the embed
     * Example:
     * "(user) (embedTitleText) (otheruser)"
     * "That's (number) (embedFooterText) now!"
     */
    abstract val embedTitleText: String

    /**
     * The footer text used in the embed
     * Example:
     * "(user) (embedTitleText) (otheruser)"
     * "That's (number) (embedFooterText) now!"
     */
    abstract val embedFooterText: String

    /**
     * When a command is issued, there's a percent chance that it will react with an emoji. This is where those emojis are stored.
     * Must be properly formatted. Ask Kopy. Defaults to a bunch of hearts.
     */
    open val possibleReactions = listOf("❤️", "💖", "💗", "💟", "❣️", "💝", "💞", "💕", "💓")

    /**
     * The percentage chance of a reaction. Set to 0 to never have any, set to 100 to always have them.
     * Default of 33
     */
    open val reactionPercent = 33

    /**
     * Because funny sex number.
     */
    open val sixtyNineGifs = listOf(
        "https://media1.tenor.com/images/552432b67854256e7b51ab96c86d8b80/tenor.gif",
    )

    final override suspend fun execute(event: MessageReceivedEvent) {
        if (event.isInDm()) {
            event.kReply("You can't use this command in DMs!")
            return
        }
        if (event.message.mentions.members.isEmpty()) {
            event.kReply("You must mention a user!")
            return
        }
        val user = event.member ?: throw CommandException("No member found!")
        val receiver = event.message.mentions.members[0]

        val button = primary("${Random.nextInt()}|${this.name}|${user.id}|${receiver.id}", "Return the $name")
        var message: Message? = null

        event.kReply(R.zeroWidthSpace)
            .setEmbeds(buildEmbed(user, receiver))
            .setActionRow(button)
            .queue {
                if (Random.nextInt(100) < reactionPercent) {
                    it.addReaction(getEmoji(possibleReactions.random())).queue()
                }
                message = it
            }

        //180000 ms is 3 minutes
        withTimeoutOrNull(180000) {
            val pressed = receiver.awaitButton(button)
            pressed.deferReply().queue()

            pressed.kReply(R.zeroWidthSpace).addEmbeds(buildEmbed(receiver, user)).queue {
                if (Random.nextInt(100) < reactionPercent) {
                    it.addReaction(getEmoji(possibleReactions.random())).queue()
                }
            }
            message!!.editMessageComponents(ActionRow.of(button.asDisabled())).queue()
        } ?: message!!.editMessageComponents(ActionRow.of(button.asDisabled())).queue()
    }

    private fun buildEmbed(user: Member, receiver: Member): MessageEmbed {
        var row: ResultRow? = null

        transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            row =
                LoveCommands.select { (LoveCommands.senderId eq user.id) and (LoveCommands.receiverId eq receiver.id) and (LoveCommands.actionId eq actionIdentifier) }
                    .firstRow()
        }

        val timesPerformed: Int
        if (row != null) {
            timesPerformed = row!![LoveCommands.timesPerformed] + 1
            transaction {
                if (R.experimental) addLogger(StdOutSqlLogger)
                LoveCommands.update({ (LoveCommands.senderId eq user.id) and (LoveCommands.receiverId eq receiver.id) and (LoveCommands.actionId eq actionIdentifier) }) {
                    it[LoveCommands.timesPerformed] = timesPerformed
                }
            }
        } else {
            //If the user has never performed this command, insert a new row
            timesPerformed = 1
            transaction {
                if (R.experimental) addLogger(StdOutSqlLogger)
                LoveCommands.insert {
                    it[senderId] = user.id
                    it[receiverId] = receiver.id
                    it[actionId] = actionIdentifier
                    it[LoveCommands.timesPerformed] = timesPerformed
                }
            }
        }

        val gif: String = if (timesPerformed.toString().contains("69")) {
            sixtyNineGifs.random()
        } else {
            gifs.random()
        }

        return Embed {
            color = R.defaultColor
            title = "${user.getNickOrUsername()} $embedTitleText ${receiver.getNickOrUsername()}"
            image = gif
            footer {
                name = "That's ${"%,d".format(timesPerformed)} $embedFooterText now!"
            }
        }
    }
}

class HugCmd : LoveCmds() {
    init {
        name = "hug"
        description = "Hugs a user"
        arguments = usage
    }

    override val actionIdentifier = "hugg"
    override val embedTitleText = "hugs"
    override val embedFooterText = "hugs"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("HugGifs").toList().map { it as String }
}

class KissCmd : LoveCmds() {
    init {
        name = "kiss"
        description = "kisses a user"
        arguments = usage
    }

    override val actionIdentifier = "kiss"
    override val embedTitleText = "kisses"
    override val embedFooterText = "kisses"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("KissGifs").toList().map { it as String }
}

class CuddleCmd : LoveCmds() {
    init {
        name = "cuddle"
        description = "Cuddles a user"
        arguments = usage
    }

    override val actionIdentifier = "cudd"
    override val embedTitleText = "cuddles"
    override val embedFooterText = "cuddles"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("CuddleGifs").toList().map { it as String }
}

class HandHoldCmd : LoveCmds() {
    init {
        name = "handhold"
        description = "Holds hands with a user"
        arguments = usage
    }

    override val actionIdentifier = "hand"
    override val embedTitleText = "holds hands with"
    override val embedFooterText = "times"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("HandHoldGifs").toList().map { it as String }
}

class HeadPatCmd : LoveCmds() {
    init {
        name = "headpat"
        description = "Pat somebody on the head!"
        arguments = usage
    }

    override val actionIdentifier = "head"
    override val embedTitleText = "headpats"
    override val embedFooterText = "headpats"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("HeadPatGifs").toList().map { it as String }
}

class BoopCmd : LoveCmds() {
    init {
        name = "boop"
        description = "Boop somebody"
        arguments = usage
    }

    override val actionIdentifier = "boop"
    override val embedTitleText = "boops"
    override val embedFooterText = "boops"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("BoopGifs").toList().map { it as String }
}

//This was a joke lmao
//class HotSteamyGaySexCmd : LoveCmds() {
//    override val name = "hotsteamygaysex"
//    override val description = "HotSteamyGaySex somebody!"
//    override val actionIdentifier = "hsgs"
//    override val embedTitleText = "HotSteamyGaySexs"
//    override val embedFooterText = "HotSteamyGaySexs"
//    override val gifs =
//        R.lists.getJSONObject("LoveCommands").getJSONArray("HotSteamyGaySexGifs").toList().map { it as String }
//    override val supportsSlash = false
//
//    override val slashCommandData: SlashCommandData
//        get() = throw CommandException("This doesnt support slash commands. Fuck you.")
//    override val textCommandData: TextCommandData =
//        TextCommandData(name, description, aliases = aliases, usage = usage)
//}