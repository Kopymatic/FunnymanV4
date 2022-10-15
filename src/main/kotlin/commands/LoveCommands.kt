package commands

import R
import dev.minn.jda.ktx.events.awaitButton
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.ActionRow
import utilities.*
import kotlin.random.Random

abstract class LoveCommands : HybridCommand() {
    open val aliases: List<String>? = null
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

    override val supportsSlash = true
    override val supportsText = true

    final override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        if (event.isInDm()) { // If the command is in a DM, we don't have anyone to send it to
            event.kReply("You can't use this command in DMs!")
            return
        }
        val user = event.member ?: throw CommandException("No member found!")
        val receiverUser = event.getOption<User>("user")!!
        val receiver: Member? = event.guild?.retrieveMemberById(receiverUser.id)?.complete()

        receiver ?: throw CommandException("Member not found")

        val button = primary("${Random.nextInt()}|${this.name}|${user.id}|${receiver.id}", "Return the $name")

        event.kReply(R.zeroWidthSpace).addEmbeds(buildEmbed(user, receiver))
            .addActionRow(button)
            .queue {
                if (Random.nextInt(100) < reactionPercent) {
                    it.addReaction(Emoji.fromFormatted(possibleReactions.random())).queue()
                }
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
            event.hook.editOriginalComponents(ActionRow.of(button.asDisabled())).queue()
        } ?: event.hook.editOriginalComponents(ActionRow.of(button.asDisabled())).queue()
    }

    final override suspend fun textCommandReceived(event: MessageReceivedEvent) {
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
        var preparedStatement = R.connection.prepareStatement(
            """
                SELECT * FROM LoveCommands WHERE SenderID = ? AND ReceiverID = ? AND ActionIdentifier = ?; 
            """.trimIndent()
        )
        preparedStatement.setString(1, user.id)
        preparedStatement.setString(2, receiver.id)
        preparedStatement.setString(3, this.actionIdentifier)
        val resultSet = preparedStatement.executeQuery()

        val timesPerformed: Int
        if (resultSet.next()) {
            timesPerformed = resultSet.getInt("TimesPerformed") + 1
            preparedStatement = R.connection.prepareStatement(
                """
                    UPDATE LoveCommands
                    SET TimesPerformed = ?
                    WHERE SenderID = ? AND ReceiverID = ? AND ActionIdentifier = ?;
                """.trimIndent()
            )
            preparedStatement.setInt(1, timesPerformed)
            preparedStatement.setString(2, user.id)
            preparedStatement.setString(3, receiver.id)
            preparedStatement.setString(4, this.actionIdentifier)
            preparedStatement.executeUpdate()
        } else {
            //If the user has never performed this command, insert a new row
            timesPerformed = 1
            preparedStatement = R.connection.prepareStatement(
                """
                INSERT INTO LoveCommands VALUES ('${user.id}', '${receiver.id}', '${this.actionIdentifier}', $timesPerformed);
                """.trimIndent()
            )
            preparedStatement.executeUpdate()
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

class HugCmd : LoveCommands() {
    override val name = "hug"
    override val description = "Hugs a user"
    override val actionIdentifier = "hugg"
    override val embedTitleText = "hugs"
    override val embedFooterText = "hugs"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("HugGifs").toList().map { it as String }

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.USER, "user", "The user to $name", true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, aliases = aliases, usage = usage)
}

class KissCmd : LoveCommands() {
    override val name = "kiss"
    override val description = "Kisses a user"
    override val actionIdentifier = "kiss"
    override val embedTitleText = "kisses"
    override val embedFooterText = "kisses"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("KissGifs").toList().map { it as String }

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.USER, "user", "The user to $name", true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, aliases = aliases, usage = usage)
}

class CuddleCmd : LoveCommands() {
    override val name = "cuddle"
    override val description = "Cuddles a user"
    override val actionIdentifier = "cudd"
    override val embedTitleText = "cuddles"
    override val embedFooterText = "cuddles"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("CuddleGifs").toList().map { it as String }

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.USER, "user", "The user to $name", true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, aliases = aliases, usage = usage)
}

class HandHoldCmd : LoveCommands() {
    override val name = "handhold"
    override val description = "Holds hands with a user"
    override val actionIdentifier = "hand"
    override val embedTitleText = "holds hands with"
    override val embedFooterText = "times"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("HandHoldGifs").toList().map { it as String }

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.USER, "user", "The user to $name", true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, aliases = aliases, usage = usage)
}

class HeadPatCmd : LoveCommands() {
    override val name = "headpat"
    override val description = "Pat somebody on the head!"
    override val actionIdentifier = "head"
    override val embedTitleText = "headpats"
    override val embedFooterText = "headpats"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("HeadPatGifs").toList().map { it as String }

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.USER, "user", "The user to $name", true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, aliases = aliases, usage = usage)
}

class BoopCmd : LoveCommands() {
    override val name = "boop"
    override val description = "Boop somebody!"
    override val actionIdentifier = "boop"
    override val embedTitleText = "boops"
    override val embedFooterText = "boops"
    override val gifs =
        R.lists.getJSONObject("LoveCommands").getJSONArray("BoopGifs").toList().map { it as String }

    override val slashCommandData: SlashCommandData =
        slash(name, description).addOption(OptionType.USER, "user", "The user to $name", true)
    override val textCommandData: TextCommandData =
        TextCommandData(name, description, aliases = aliases, usage = usage)
}