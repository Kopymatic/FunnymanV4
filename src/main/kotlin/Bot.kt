import commands.AllCommands
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import utilities.EverythingListener
import utilities.kReply
import kotlin.system.exitProcess

val log = R.log

/*
args[0] = Token
args[1] = DB URL
args[2] = DB Username
args[3] = DB Password
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        log.error("You have to provide a token as first argument, and database information for the 2nd 3rd and 4th!")
        exitProcess(1)
    }
    R.connection = R.connect(args[1], args[2], args[3])

    log.info(
        """
                --Configuration--
                    Experimental: ${R.experimental}
                    Version: ${R.version}
                    ${if (R.experimental) R.debugGuild else ""}
            """.trimIndent()
    )
    log.info("Creating JDA instance...")
    R.jda = light(
        args[0],
        enableCoroutines = true,
        builder = { this.enableIntents(GatewayIntent.MESSAGE_CONTENT) }).awaitReady()
    R.jda.addEventListener(EverythingListener())

    log.info("Getting debug channel")
    R.debugChannel = R.jda.getChannelById(TextChannel::class.java, R.debugChannelId)!!

    log.debug("Getting commands...")
    val commands = AllCommands.commands

    log.info("Registering message listener...")
    R.jda.listener<MessageReceivedEvent> {
        var hasPrefix = false
        var name: String? = null
        for (prefix in R.prefixes) {
            if (it.message.contentRaw.startsWith(prefix)) {
                hasPrefix = true
                name = it.message.contentRaw.substring(prefix.length).split(" ")[0]
                break
            }
        }
        if (!hasPrefix) return@listener
        if (name == null) return@listener

        for (command in commands) {
            if (command.supportsText && (command.textCommandData.name.lowercase() == name.lowercase()) || (command.textCommandData.aliases != null && command.textCommandData.aliases!!.contains(
                    name.lowercase()
                ))
            ) {
                try {
                    command.textCommandReceived(it)
                } catch (e: Exception) {
                    it.kReply("An internal exception occurred: \"${e.message}\"").queue()
                    logError(e)
                }
                break
            }
        }
    }

    log.info("Registering slash listener...")
    R.jda.listener<SlashCommandInteractionEvent> {
        it.deferReply().await()
        for (command in commands) {
            if (command.supportsSlash && command.slashCommandData.name == it.name) {
                try {
                    command.slashCommandReceived(it)
                } catch (e: Exception) {
                    it.kReply("An internal exception occurred: \"${e.message}\"").setEphemeral(true).queue()
                    logError(e)
                }
                break
            }
        }
    }

    log.info("Registering autocomplete listener...")
    R.jda.listener<CommandAutoCompleteInteractionEvent> {
        for (command in commands) {
            if (command.slashCommandData.name == it.name) {
                command.onAutoComplete(it)
                break
            }
        }
    }

    log.info("Registering slash commands...")
    if (R.experimental) {
        val guild = R.jda.getGuildById(R.debugGuild)!!
        for (command in commands) {
            if (command.supportsSlash) {
                log.info("Registering guild command ${command.slashCommandData.name}...")
                guild.upsertCommand(command.slashCommandData).queue()
            }
        }
    } else {
        for (command in commands) {
            if (command.supportsSlash) {
                log.info("Registering command ${command.slashCommandData.name}...")
                R.jda.upsertCommand(command.slashCommandData).queue()
            }
        }
    }

    R.jda.presence.activity =
        Activity.watching("Version ${R.version} ${if (R.experimental) "Experimental" else ""}")
}

fun logError(error: Exception) {
    R.log.error(error.message)
    R.debugChannel.sendMessageEmbeds(Embed {
        title = "Error: \"${error.message}\""
    }).queue()
}