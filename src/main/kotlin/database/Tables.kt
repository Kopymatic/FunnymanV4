package database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object LoveCommands : Table() {
    val senderId: Column<String> = text("senderid")
    val receiverId: Column<String> = text("receiverid")
    val actionId: Column<String> = text("actionidentifier")
    val timesPerformed: Column<Int> = integer("timesperformed")
    val key: Column<Int> = integer("primarykey").autoIncrement()
    override val primaryKey = PrimaryKey(key)
}

object Guilds : IntIdTable() {
    val guildId: Column<String> = text("guildId").uniqueIndex()
    val data: Column<String> = text("data")
}