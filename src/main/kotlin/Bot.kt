import commands.AllCommands
import database.createTables
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.jetbrains.exposed.sql.Database
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
    R.database = Database.connect(args[2], "org.postgresql.Driver", args[3], args[4])

    createTables()

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


    //TODO: rewrite this shit
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
            if ((command.name.lowercase() == name.lowercase()) || (command.aliases != null && command.aliases!!.contains(
                    name.lowercase()
                ))
            ) {
                try {
                    log.info("Command received: ${command.name}")
                    Coroutines.main { command.execute(it) }
                } catch (e: Exception) {
                    it.kReply("An internal exception occurred: \"${e.message}\"").queue()
                    logError(e)
                }
                break
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