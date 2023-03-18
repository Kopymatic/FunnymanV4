package commands

//TODO: Rethink the implementation of Phrases
//
//import R
//import dev.minn.jda.ktx.interactions.commands.Subcommand
//import dev.minn.jda.ktx.messages.Embed
//import net.dv8tion.jda.api.entities.MessageEmbed
//import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent
//import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
//import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
//import utilities.HybridCommand
//import utilities.TextCommandData
//import utilities.kReply
//
//class PhraseCommands : HybridCommand() {
//    override val name: String = "phrase"
//    override val description: String = "make the bot say a phrase"
//
//
//    override val supportsSlash: Boolean = true
//    override val supportsText: Boolean = true
//    override val slashCommandData: SlashCommandData = slash(name, description).addSubcommands(
//        Subcommand("lenny", "Sends a lenny emote"),
//        Subcommand("hmmm", "Sends a concerned lenny emote")
//    )
//    override val textCommandData: TextCommandData = TextCommandData(
//        name,
//        description,
//        aliases = listOf("p"),
//        usage = "Do \"${R.prefixes[0]}$name\" or \"ppp\" with no arguments for help"
//    )
//
//    override suspend fun slashCommandReceived(event: SlashCommandInteractionEvent) {
//        when (event.subcommandName) {
//            "lenny" -> event.kReply("( ͡° ͜ʖ ͡°)").queue()
//            "hmmm" -> event.kReply("( ͠° ͟ʖ ͡°)").queue()
//        }
//    }
//
//    override suspend fun textCommandReceived(event: MessageReceivedEvent) {
//        val args = event.message.getArgs()
//        if (args[0].isBlank()) {
//            event.kReply().addEmbeds(getHelp()).queue()
//        } else if (args[0].lowercase() == "lenny") {
//            event.kReply("( ͡° ͜ʖ ͡°)").queue()
//        } else if (args[0].lowercase() == "hmmm") {
//            event.kReply("( ͠° ͟ʖ ͡°)").queue()
//        }
//    }
//
//    private fun getHelp(): MessageEmbed {
//        return Embed {
//            title = "Phrase help"
//            description =
//                "Here's all the available phrases. Eventually, if interest is high enough, you may be able to import your own phrases"
//            field {
//                name = "lenny"
//                description = "( ͡° ͜ʖ ͡°)"
//            }
//            field {
//                name = "hmmm"
//                description = "( ͠° ͟ʖ ͡°)"
//            }
//        }
//    }
//}