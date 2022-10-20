package database

import R
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun createTables() {
    R.log.info("Creating tables...")
    transaction {
        if (R.experimental) addLogger(StdOutSqlLogger)
        SchemaUtils.create(LoveCommands)
        SchemaUtils.create(NoContext)
        SchemaUtils.create(People)
        SchemaUtils.create(Pets)
        SchemaUtils.create(Memes)
        SchemaUtils.create(Guilds)
    }
    R.log.info("Tables created!")
}

object LoveCommands : Table() {
    val senderId: Column<String> = text("senderid")
    val receiverId: Column<String> = text("receiverid")
    val actionId: Column<String> = text("actionidentifier")
    val timesPerformed: Column<Int> = integer("timesperformed")
    val key: Column<Int> = integer("primarykey").autoIncrement()
    override val primaryKey = PrimaryKey(key)
}

open class RandomImageCommandsDb : IntIdTable() {
    val guildId: Column<String> = text("guildid")
    val imageLink: Column<String> = text("imagelink")
    val linkTag: Column<String?> = text("linktag").nullable()
    val textTag: Column<String?> = text("texttag").nullable()
    val importerId: Column<String> = text("importerid")
    val importMessageId: Column<String> = text("importmessageid")
}

object NoContext : RandomImageCommandsDb()

object People : RandomImageCommandsDb()

object Pets : RandomImageCommandsDb()

object Memes : RandomImageCommandsDb()


object Guilds : IntIdTable() {
    val guildId: Column<String> = text("guildId").uniqueIndex()
    val data: Column<String> = text("data")
}

//https://stackoverflow.com/questions/52930722/psql-case-insensitive-search-using-exposed
class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> =
    ILikeOp(this, QueryParameter(pattern, columnType))