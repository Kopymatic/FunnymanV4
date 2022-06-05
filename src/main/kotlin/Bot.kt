import commands.AllCommands
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.system.exitProcess

val log = Reference.log

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
    Reference.connection = Reference.connect(args[1], args[2], args[3])
    Bot.init(args[0])
}

class Bot : ListenerAdapter() {
    companion object {
        fun init(token: String) {
            log.info("Creating JDA instance...")
            Reference.jda = light(token, enableCoroutines = true).awaitReady()

            log.debug("Getting commands...")
            val commands = AllCommands.commands

            log.info("Registering message listener...")
            Reference.jda.listener<MessageReceivedEvent> {
                var hasPrefix = false
                var name: String? = null
                for (prefix in Reference.prefixes) {
                    if (it.message.contentRaw.startsWith(prefix)) {
                        hasPrefix = true
                        name = it.message.contentRaw.substring(prefix.length).split(" ")[0]
                        break
                    }
                }
                if (!hasPrefix) return@listener
                if (name == null) return@listener

                for (command in commands) {
                    if (command.supportsText && (command.textCommandData.name.lowercase() == name.lowercase()) || (command.textCommandData.aliases != null && command.textCommandData.aliases!!.contains(name.lowercase()))) {
                        command.textCommandReceived(it)
                        break
                    }
                }
            }

            log.info("Registering slash listener...")
            Reference.jda.listener<SlashCommandInteractionEvent> {
                it.deferReply().await()
                for (command in commands) {
                    if (command.supportsSlash && command.slashCommandData.name == it.name) {
                        command.slashCommandReceived(it)
                        break
                    }
                }
            }

            log.info("Registering autocomplete listener...")
            Reference.jda.listener<CommandAutoCompleteInteractionEvent> {
                for (command in commands) {
                    if (command.slashCommandData.name == it.name) {
                        command.onAutoComplete(it)
                        break
                    }
                }
            }

            log.info("Registering slash commands...")
            if (Reference.experimental) {
                val guild = Reference.jda.getGuildById(Reference.debugGuild)!!
                for (command in commands) {
                    log.info("Registering guild command ${command.slashCommandData.name}...")
                    if (command.supportsSlash) guild.upsertCommand(command.slashCommandData).queue()
                }
            } else {
                for (command in commands) {
                    log.info("Registering command ${command.slashCommandData.name}...")
                    if (command.supportsSlash) Reference.jda.upsertCommand(command.slashCommandData).queue()
                }
            }

            Reference.jda.presence.activity = Activity.watching("Version ${Reference.version} ${if (Reference.experimental) "Experimental" else ""}")
        }
    }
}