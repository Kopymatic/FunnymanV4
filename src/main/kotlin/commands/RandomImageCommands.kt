package commands

import R
import database.*
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Subcommand
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import utilities.*

abstract class RandomImageCommands<T : RandomImageCommandsDb> : HybridCommand() {
    override val supportsSlash = true
    override val supportsText = true

    /**
     * The name of the database table to store this command's info inside - this MUST BE SET
     */
    abstract val database: T

    /**
     * The footers this command can show - this MUST BE SET
     */
    abstract val footers: Array<String>

    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
        //Return if not in a guild
        if (event.guild == null) {
            event.kReply("This command can only be used in a server.")
            return
        }

        when (event.subcommandName) {
            "random" -> {
                event.kReply().addEmbeds(random(event.guild!!.id)).queue()
            }
            "find" -> {
                event.kReply("Loading...").queue {
                    ButtonPaginator(
                        R.jda,
                        it,
                        ButtonPaginatorOptions(
                            find(
                                event.getOption<Int?>("id"),
                                event.getOption<String?>("tag"),
                                event.guild!!.id
                            ),
                            0
                        )
                    )
                }
            }
            "import" -> {
                event.channel.sendMessage(R.zeroWidthSpace).setEmbeds(importSlash(event)).queue()
            }
            "edit" -> {
                event.kReply().addEmbeds(
                    edit(
                        event.getOption<Int>("id")!!,
                        event.getOption<String>("description")!!,
                        event.guild!!.id,
                        event.member!!
                    )
                ).queue()
            }
            "delete" -> {
                event.kReply().addEmbeds(delete(event.getOption<Int>("id")!!, event.guild!!.id, event.member!!)).queue()
            }
            else -> {
                event.kReply("Unknown subcommand").queue()
            }
        }
    }

    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
        val args = event.message.getArgs()
        when {
            event.message.attachments.isNotEmpty() -> {
                if (!event.message.attachments[0].isImage) {
                    event.kReply("This is not an image.").queue()
                    return
                }
                event.kReply()
                    .setEmbeds(
                        import(
                            event.message.attachments[0].url,
                            args.joinToString(" "),
                            event.guild.id,
                            event.author.id,
                            event.messageId
                        )
                    ).queue()
            }
            args[0].trim().equals("edit", true) -> {
                val id = args[1].toInt()
                val description = args.drop(2).joinToString(" ")
                event.kReply().setEmbeds(edit(id, description, event.guild.id, event.member!!)).queue()
            }
            args[0].trim().equals("delete", true) -> {
                try {
                    event.kReply().setEmbeds(delete(args[1].toInt(), event.guild.id, event.member!!)).queue()
                } catch (e: NumberFormatException) {
                    event.kReply("Invalid ID").queue()
                    return
                }
            }
            args[0].isBlank() -> event.kReply().setEmbeds(random(event.guild.id)).queue()
            args[0].trim().equals("help", true) -> {
                event.kReply().setEmbeds(getAdvancedHelp().build()).queue()
            }
            else -> {
                val text = args.joinToString(" ")
                try {
                    val int = text.trim().toInt()
                    event.kReply("Loading...").queue {
                        ButtonPaginator(
                            R.jda,
                            it,
                            ButtonPaginatorOptions(
                                find(int, null, event.guild.id),
                                0
                            )
                        )
                    }
                } catch (e: NumberFormatException) {
                    event.kReply("Loading...").queue {
                        ButtonPaginator(
                            R.jda,
                            it,
                            ButtonPaginatorOptions(
                                find(null, text, event.guild.id),
                                0
                            )
                        )
                    }
                }
            }
        }
    }

    private fun random(guildId: String): MessageEmbed {
        var row: ResultRow? = null
        transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            row = database.select { database.guildId eq guildId }.orderBy(Random()).limit(1).firstRow()
        }

        if (row == null) {
            return Embed(
                color = R.red,
                description = "Error: Likely you have nothing imported in this server, or a database error has occurred."
            )
        }

        return makeEmbed(row!!)
            ?: throw CommandException("Unable to make embed - returned embed was null")
    }

    private fun find(id: Int?, tags: String?, guildId: String): List<MessageEmbed> {
        if (id != null) {
            val dbSize = getBiggestId()
            //TODO: Add partnering
            var row: ResultRow? = null

            transaction {
                if (R.experimental) addLogger(StdOutSqlLogger)
                row = database.select { (database.id eq id) and (database.guildId eq guildId) }.firstRow()
            }

            if (row == null) {
                return listOf(
                    Embed(
                        description = "This entry is unavailable! Its likely it was deleted or is out of the database range",
                        footerText = "The highest ID is $dbSize, but some may be unavailable due to deleting or being in different guilds",
                        color = R.red
                    )
                )
            }
            return listOf(
                makeEmbed(row!!) ?: throw CommandException("Unable to return embed - makeEmbed returned null")
            )
        } else if (tags != null) {
            val embeds = mutableListOf<MessageEmbed>()

            transaction {
                if (R.experimental) addLogger(StdOutSqlLogger)
                val query = if (tags.trim().equals("all", true)) {
                    database.select { database.guildId eq guildId }
                } else {
                    database.select { (database.textTag ilike "%$tags%") and (database.guildId eq guildId) }
                }

                query.forEach {
                    val embed = makeEmbed(it)
                    if (embed != null) {
                        embeds.add(embed)
                    }
                }
            }

            if (embeds.isEmpty()) {
                return listOf(
                    Embed(
                        description = "No entries found with that tag!",
                        footerText = "The highest ID is ${getBiggestId()}, but some may be unavailable due to deleting or being in different guilds",
                        color = R.red
                    )
                )
            }

            return embeds
        } else {
            return listOf(
                Embed(
                    description = "Error: You must provide something to search for!",
                    color = R.red
                )
            )
        }
    }

    private suspend fun importSlash(event: SlashCommandInteractionEvent): MessageEmbed {
        val attachment = event.getOption<Attachment>("image")!!
        if (!attachment.isImage) {
            return Embed(
                color = R.red,
                description = "Error: This is not an image!"
            )
        }
        //FIXME: Stop using deprecated function
        val file = attachment.downloadToFile().await().absoluteFile
        val message = event.kReply(
            "Importing image... " +
                    "\n Do NOT delete this message. It will delete the image from Discord's servers and it will no longer be seen in the command"
        ).addFiles(FileUpload.fromData(file)).await()
        file.delete()
        val url = message.attachments[0].url
        val messageId = message.id
        return import(url, event.getOption<String>("description")!!, event.guild!!.id, event.user.id, messageId)
    }

    private fun import(
        attachmentUrl: String,
        description: String,
        guildId: String,
        memberId: String,
        messageId: String
    ): MessageEmbed {
        val textTag: String = description

        transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            database.insert {
                it[database.guildId] = guildId
                it[database.imageLink] = attachmentUrl
                it[database.linkTag] = null
                it[database.textTag] = textTag
                it[database.importerId] = memberId
                it[database.importMessageId] = messageId
            }
        }
        try {
            val rs = getLatestEntry()

            return Embed(
                color = R.green,
                title = "Successfully imported image with id ${rs[database.id]}",
                description = "Description: ${rs[database.textTag]}",
                image = rs[database.imageLink]
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Embed(
                color = R.red,
                description = "Error: Unable to import image."
            )
        }
    }

    private fun edit(toEdit: Int, tags: String, guildId: String, member: Member): MessageEmbed {
        var row: ResultRow? = null

        transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            row = database.select { database.id eq toEdit }.firstRow()
        }

        if (row != null) {
            if (row!![database.guildId] != guildId) {
                return Embed(
                    color = R.red,
                    description = "Error: This ID isn't available in this guild!"
                )
            }

            if (!member.permissions.contains(Permission.ADMINISTRATOR) && member.id != row!![database.importerId]) {
                return Embed(
                    color = R.red,
                    description = "Error: You must either be a server administrator or the original importer to edit this!"
                )
            }

            val textTag: String = tags
            val linkTag: String? = null

            var result: Int? = null
            transaction {
                if (R.experimental) addLogger(StdOutSqlLogger)
                result = database.update({ database.id eq toEdit }) {
                    it[database.textTag] = textTag
                    it[database.linkTag] = linkTag
                }
            }

            return if (result == null || result == 0) {
                //Unsuccessful if result is null or 0
                throw CommandException("Unable to edit entry -- Contact Kopy#1424")
            } else if (result == 1) {
                //Successful if result is 1
                Embed(
                    color = R.green,
                    description = "Successfully edited tags of $toEdit to $tags"
                )
            } else {
                //What the hell happened here
                throw CommandException("What happened here??")
            }
        } else {
            return Embed(
                color = R.red,
                description = "Error: Invalid number! Either not a valid ID or out of database range!"
            )
        }
    }

    private fun delete(toDelete: Int, guildId: String, member: Member): MessageEmbed {
        var row: ResultRow? = null

        transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            row = database.select { database.id eq toDelete }.firstRow()
        }

        if (row != null) {
            if (row!![database.guildId] != guildId) {
                return Embed(
                    color = R.red,
                    description = "Error: You cannot delete an entry that is not in this guild!"
                )
            }

            if (!member.permissions.contains(Permission.ADMINISTRATOR) && member.id != row!![database.importerId]) {
                return Embed(
                    color = R.red,
                    description = "Error: You cannot delete an entry that isn't yours unless you're an administrator!"
                )
            }

            var result: Int? = null
            transaction {
                if (R.experimental) addLogger(StdOutSqlLogger)
                result = database.deleteWhere { database.id eq toDelete }
            }

            return if (result == null || result == 0) {
                //Unsuccessful if result is null or 0
                throw CommandException("Unable to delete entry -- Contact Kopy#1424")
            } else if (result == 1) {
                //Successful if result is 1
                Embed(
                    color = R.green,
                    description = "Success!"
                )
            } else {
                //What the hell happened here
                throw CommandException("What happened here??")
            }
        } else {
            return Embed(
                color = R.red,
                description = "Invalid number! Either not a valid ID or out of database range!"
            )
        }
    }

    private fun makeEmbed(resultRow: ResultRow): MessageEmbed? {
        val id = resultRow[database.id]
        val textTag = resultRow[database.textTag]
        val linkTag = resultRow[database.linkTag]
        val image = resultRow[database.imageLink]

        val invalidEndings = arrayOf(".mp4")
        var isInvalid = false
        for (ending in invalidEndings) {
            if (image.endsWith(ending, true)) {
                isInvalid = true
                break
            }
        }
        if (isInvalid) {
            return null
        }
        var descText: String? = "[$textTag]($linkTag)"
        var url: String? = null

        if (textTag.equals("NULL", true) && !linkTag.equals("NULL", true)) {
            url = linkTag
            descText = null
        }

        if (linkTag == "NULL") {
            descText = textTag
        }

        return (Embed(
            title = "${this.name} #$id",
            url = url,
            description = descText,
            image = image,
            footerText = this.footers.random(),
            color = R.defaultColor
        ))
    }

    private fun getLatestEntry(): ResultRow {
        var latestEntry: ResultRow? = null
        transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            latestEntry = database.selectAll().orderBy(database.id to SortOrder.DESC).limit(1).firstRow()
        }
        return latestEntry ?: throw CommandException("latestEntry isn't supposed to be null!")
    }

    private fun getBiggestId(): Int {
        val id = transaction {
            if (R.experimental) addLogger(StdOutSqlLogger)
            database
                .slice(database.id.max())
                .selectAll()
                .firstOrNull()
                ?.get(database.id.max())?.value
        }
        return id ?: throw CommandException("Biggest ID was null")
    }

    private fun getAdvancedHelp(): EmbedBuilder {
        return EmbedBuilder()
            .setTitle("How to use ${this.name}:")
            .setDescription("${this.name} is a simple command with many complex operations you can do. Here's an explanation.")
            .addField(
                "Getting a random ${this.name} entry:",
                "This is as simple as running the command with no arguments",
                false
            )
            .addField(
                "Getting a specific ${this.name} entry:",
                "Run the command with a search or an entry id to get a specific entry" +
                        "\n**Examples:** `${R.prefixes[0]}${this.name} (search term)` or `${R.prefixes[0]}${this.name} (entry id)`",
                false
            )
            .addField(
                "Importing:",
                "To import an image to the database, send the command with an attachment. Optionally, you can supply a description." +
                        "\n**Note:** Videos are **not** currently supported.",
                false
            )
            .addField(
                "Editing:",
                "If you ever wish to edit the text of something you previously imported, " +
                        "send the command the same as you would with importing, but with edit and an id at the beginning and no attachment" +
                        "\n**Example:** `${R.prefixes[0]}${this.name} edit (entry ID) (new text here)`" +
                        "\n**Note:** Editing an import that has a link with no link will delete that link.", false
            )
            .addField(
                "Deleting:",
                "If you wish to delete something you imported, just run the command with delete and an id" +
                        "\n**Example**: `${R.prefixes[0]}${this.name} delete (entry ID)`",
                false
            )
    }

    protected fun setUpOptions(data: SlashCommandData): SlashCommandData {
        return data.addSubcommands(Subcommand("random", "Shows an image that was imported to the database"))
            .addSubcommands(Subcommand("find", "Finds an image from the database") {
                addOption(OptionType.STRING, "tag", "The tag to search for", false)
                addOption(OptionType.INTEGER, "id", "The id to search for", false)
            })
            .addSubcommands(Subcommand("import", "Adds an image to the database") {
                addOption(OptionType.ATTACHMENT, "image", "The image to add to the database", true)
                addOption(OptionType.STRING, "description", "A description of the image", true)
            })
            .addSubcommands(Subcommand("edit", "Edits an image from the database") {
                addOption(OptionType.INTEGER, "id", "The id of the image to edit", true)
                addOption(OptionType.STRING, "description", "A description of the image", true)
            })
            .addSubcommands(Subcommand("delete", "Removes an image from the database") {
                addOption(OptionType.INTEGER, "id", "The id of the image to remove from the database", true)
            })
    }
}

class NoContextCmd : RandomImageCommands<NoContext>() {
    override val name = "nocontext"
    override val description = "No context"

    override val database = NoContext
    override val footers: Array<String> = arrayOf("Laugh. Now.", "laugh! >:(", "nice meme, very poggers")
    override val slashCommandData = setUpOptions(slash(name, description))
    override val textCommandData =
        TextCommandData(
            name,
            description,
            aliases = listOf("nc"),
            usage = "Do \"${R.prefixes[0]}$name help\" for help"
        )
}

class PeopleCmd : RandomImageCommands<People>() {
    override val name = "people"
    override val description = "People"

    override val database = People
    override val footers =
        arrayOf("Oh this- this is beautiful", "Looking fabulous!", "that's a cute ass person ya got there")

    override val slashCommandData = setUpOptions(slash(name, description))
    override val textCommandData =
        TextCommandData(
            name,
            description,
            aliases = listOf("me"),
            usage = "Do \"${R.prefixes[0]}$name help\" for help"
        )
}

class PetCmd : RandomImageCommands<Pets>() {
    override val name = "pet"
    override val description = "Pets!"


    override val database = Pets
    override val footers: Array<String> = arrayOf(
        "Oh this- this is beautiful",
        "Looking fabulous!",
        "aww cute pet",
        "that's a cute ass pet ya got there"
    )
    override val slashCommandData = setUpOptions(slash(name, description))
    override val textCommandData =
        TextCommandData(
            name,
            description,
            usage = "Do \"${R.prefixes[0]}$name help\" for help"
        )
}

class MemeCmd : RandomImageCommands<Memes>() {
    override val name = "meme"
    override val description = "Funny funny haha memes"

    override val database = Memes
    override val footers: Array<String> = arrayOf("haha funny", "nice meme, very poggers", "laugh! >:(")
    override val slashCommandData = setUpOptions(slash(name, description))
    override val textCommandData =
        TextCommandData(
            name,
            description,
            usage = "Do \"${R.prefixes[0]}$name help\" for help"
        )
}