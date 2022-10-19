import commands.AllCommands
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import utilities.Coroutines
import utilities.EverythingListener
import utilities.kReply
import kotlin.system.exitProcess

val log = R.log

/*
args[0] = Token
args[1] = Experimental (True or False)
args[2] = DB URL
args[3] = DB Username
args[4] = DB Password
args[5] = Create tables (True or False)
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        log.error("index 0 should be the token, 1 should be experimental (true or false), 2 should be db url, 3 is db username, and 4 is db pass")
        exitProcess(1)
    }

    val token = args[0]
    R.experimental = args[1].toBoolean()
    R.prefixes = if (R.experimental) listOf("dd", "d!.") else listOf("pp", "p!")
    R.connection = R.connect(args[2], args[3], args[4])

    log.info(
        """
                --Configuration--
                    Experimental: ${R.experimental}
                    Version: ${R.version}
                    ${if (R.experimental) "Debug guild: ${R.debugGuild}" else ""}
            """.trimIndent()
    )

    //Create the JDA instance
    log.info("Creating JDA instance...")
    R.jda = light(
        token,
        enableCoroutines = true,
        builder = { this.enableIntents(GatewayIntent.MESSAGE_CONTENT) }).awaitReady()
    R.jda.addEventListener(EverythingListener())

    log.info("Getting debug channel")
    R.debugChannel = R.jda.getChannelById(TextChannel::class.java, R.debugChannelId)!!

    val commands = AllCommands.commands

    log.info("Registering message listener...")
    R.jda.listener<MessageReceivedEvent> {
        var hasPrefix = false
        var name: String? = null
        for (prefix in R.prefixes) {
            if (it.message.contentRaw.lowercase().startsWith(prefix)) {
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
                    log.info("Command received: ${command.textCommandData.name}")
                    Coroutines.main { command.textCommandReceived(it) }
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
        log.info("Slash command received: ${it.name}")
        it.deferReply().await()
        for (command in commands) {
            if (command.supportsSlash && command.slashCommandData.name == it.name) {
                try {
                    Coroutines.main {
                        command.slashCommandReceived(it)
                    }
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
                Coroutines.main {
                    command.onAutoComplete(it)
                }
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

    R.jda.presence.activity = Activity.watching("V${R.version} ${if (R.experimental) "Experimental" else ""}")
}

fun logError(error: Exception) {
    R.log.error(error.message)
    R.debugChannel.sendMessageEmbeds(Embed {
        title = "Error: \"${error.message}\""
    }).queue()
}