import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.TextChannel
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.system.exitProcess

class R {
    companion object {
        const val experimental = true

        const val version = "4.0.0 PTB 5"
        val log = LoggerFactory.getLogger("Main") as Logger
        val prefixes = if (experimental) listOf("dd", "d!.") else listOf("pp", "p!")
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

        lateinit var connection: Connection

        fun connect(url: String, username: String, password: String): Connection {
            try {
                val conn = DriverManager.getConnection(url, username, password)
                log.info("Connected to the PostgreSQL server successfully.")
                return conn
            } catch (e: SQLException) {
                log.error("The database failed to connect, exiting process")
                log.error(e.message)
                exitProcess(0)
            }
        }
    }
}
