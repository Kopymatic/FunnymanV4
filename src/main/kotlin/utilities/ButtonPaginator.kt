package utilities

import Reference
import dev.minn.jda.ktx.interactions.components.button
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ButtonPaginator(
    private var client: JDA,
    private var message: Message,
    private var options: ButtonPaginatorOptions
) {
    private var skipForwardButton: Button
    private var skipBackButton: Button
    private var forwardButton: Button
    private var backButton: Button
    private var expiration: Duration = options.expiration
    private var currentPage: Int = options.startingPage

    init {
        this.backButton = this.client.button(ButtonStyle.PRIMARY, emoji = Emoji.fromUnicode("◀"), expiration = expiration) {
            if (this.currentPage <= 0) {
                this.currentPage = this.options.pages.size - 1
            } else {
                this.currentPage--
            }
            this.updateMessage()
        }
        this.forwardButton = this.client.button(ButtonStyle.PRIMARY, emoji = Emoji.fromUnicode("▶"), expiration = expiration) {
            if (this.currentPage >= this.options.pages.size - 1) {
                this.currentPage = 0
            } else {
                this.currentPage++
            }
            this.updateMessage()
        }
        this.skipBackButton = this.client.button(ButtonStyle.PRIMARY, emoji = Emoji.fromUnicode("⏪"), expiration = expiration) {
            if (this.currentPage - 10 < 0) {
                this.currentPage = this.options.pages.size - 1
            } else {
                this.currentPage -= 10
            }
            this.updateMessage()
        }
        this.skipForwardButton = this.client.button(ButtonStyle.PRIMARY, emoji = Emoji.fromUnicode("⏩"), expiration = expiration) {
            if (this.currentPage + 10 > this.options.pages.size - 1) {
                this.currentPage = 0
            } else {
                this.currentPage += 10
            }
            this.updateMessage()
        }

        updateMessage()
    }

    private fun updateMessage() {
        this.message.editMessage(Reference.zeroWidthSpace).setEmbeds(options.pages[currentPage])
            .setActionRow(skipBackButton, backButton, forwardButton, skipForwardButton).queue()
    }
}


data class ButtonPaginatorOptions(
    val pages: List<MessageEmbed>,
    val startingPage: Int = 0,
    val expiration : Duration = 1.minutes,
)
