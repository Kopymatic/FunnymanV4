import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.jetbrains.exposed.sql.Database
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class R {
    companion object {
        var experimental = true

        const val version = "4.0.0 A10"
        val log = LoggerFactory.getLogger("Main") as Logger
        lateinit var prefixes: List<String>
        lateinit var jda: JDA
        const val debugGuild = "793293945437814797"
        const val debugChannelId = "826674337591197708"
        lateinit var debugChannel: TextChannel
        val lists = JSONObject(this::class.java.getResource("lists.json")?.readText())
        const val zeroWidthSpace = "\u200B"
        const val defaultColor: Int = 0xFF00FF
        const val red: Int = 0xFF0000
        const val green: Int = 0x00FF00
        val owners = listOf("326489320980611075")
        lateinit var database: Database
    }
}
