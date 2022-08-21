package utilities
//
//fun main(args: Array<String>) {
//    R.connection = R.connect(args[1], args[2], args[3])
//
//    create( //Create LoveCommands
//        """
//            CREATE TABLE LoveCommands(
//            SenderID TEXT NOT NULL,
//            ReceiverID TEXT NOT NULL,
//            ActionIdentifier TEXT NOT NULL,
//            TimesPerformed INT NOT NULL,
//            primaryKey SERIAL PRIMARY KEY
//            );
//        """.trimIndent()
//    )
//
//    create( //Create NoContext
//        """
//                CREATE TABLE NoContext(
//                    id SERIAL PRIMARY KEY,
//                    guildID TEXT NOT NULL,
//                    ImageLink TEXT NOT NULL,
//                    LinkTag TEXT,
//                    TextTag TEXT,
//                    ImporterID TEXT NOT NULL,
//                    ImportMessageID TEXT NOT NULL
//                );
//            """.trimIndent()
//    )
//
//    create( //Create People
//        """
//                CREATE TABLE People(
//                    id SERIAL PRIMARY KEY,
//                    guildID TEXT NOT NULL,
//                    ImageLink TEXT NOT NULL,
//                    LinkTag TEXT,
//                    TextTag TEXT,
//                    ImporterID TEXT NOT NULL,
//                    ImportMessageID TEXT NOT NULL
//                );
//            """.trimIndent()
//    )
//
//    create( //Create Pets
//        """
//                CREATE TABLE Pets(
//                    id SERIAL PRIMARY KEY,
//                    guildID TEXT NOT NULL,
//                    ImageLink TEXT NOT NULL,
//                    LinkTag TEXT,
//                    TextTag TEXT,
//                    ImporterID TEXT NOT NULL,
//                    ImportMessageID TEXT NOT NULL
//                );
//            """.trimIndent()
//    )
//
//    create( //Create Meme
//        """
//                CREATE TABLE Memes(
//                    id SERIAL PRIMARY KEY,
//                    guildID TEXT NOT NULL,
//                    ImageLink TEXT NOT NULL,
//                    LinkTag TEXT,
//                    TextTag TEXT,
//                    ImporterID TEXT NOT NULL,
//                    ImportMessageID TEXT NOT NULL
//                );
//            """.trimIndent()
//    )
//
//    create( //Create dayLogger
//        """
//            CREATE TABLE DayLogger (
//               logTime TIMESTAMP NOT NULL,
//               userID TEXT NOT NULL,
//               logRating INT NOT NULL,
//               logTextRating TEXT,
//               logSummary TEXT
//            );
//        """.trimIndent()
//    )
//
//    create( //Create guildSettings //DEFAULTCOLOR IS USING COLOR.RGB
//        """
//            CREATE TABLE GuildSettings (
//               guildID TEXT PRIMARY KEY,
//               defaultColor INT NOT NULL DEFAULT -36865,
//               partneredGuilds TEXT,
//               doSexAlarm BOOLEAN NOT NULL DEFAULT true,
//               dylanMode boolean NOT NULL DEFAULT false
//            );
//        """.trimIndent()
//    )
//
//    update(
//        """
//        ALTER TABLE GuildSettings ADD JoeMode BOOLEAN DEFAULT FALSE;
//    """.trimIndent()
//    )
//}
//
//fun create(sql: String) {
//    try {
//        val c = R.connection
//        val stmt = c.createStatement()
//        stmt.executeUpdate(sql)
//        stmt.close()
//        c.close()
//        println("Success!")
//    } catch (e: Exception) {
//        println(e.message)
//    }
//}
//
//fun update(sql: String) {
//    try {
//        val c = R.connection
//        val stmt = c.createStatement()
//        stmt.executeUpdate(sql)
//        stmt.close()
//        c.close()
//        println("Edit Success!")
//    } catch (e: Exception) {
//        println(e.message)
//    }
//}