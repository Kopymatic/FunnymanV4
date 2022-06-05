package utilities

data class TextCommandData(
    val name: String,
    val description: String,
    val aliases: List<String>? = null,
    val usage: String? = null,
    val category: String? = null,
)