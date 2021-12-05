package com.theost.workchat.elm.dialog

import android.text.SpannableString
import androidx.core.text.HtmlCompat
import com.theost.workchat.data.models.core.Message
import com.theost.workchat.data.models.core.Reaction
import com.theost.workchat.data.models.state.MessageType
import com.theost.workchat.data.models.ui.ListDate
import com.theost.workchat.data.models.ui.ListMessage
import com.theost.workchat.data.models.ui.ListMessageReaction
import com.theost.workchat.ui.interfaces.DelegateItem
import com.theost.workchat.utils.DateUtils

object DialogItemHelper {

    fun replaceMessage(
        oldMessages: List<ListMessage>,
        newMessage: Message,
        reactions: List<Reaction>,
        currentUserId: Int
    ): List<ListMessage> {
        return oldMessages.toMutableList().apply {
            val index = oldMessages.indexOfFirst { message -> message.id == newMessage.id }
            removeAt(index)
            addAll(index, mapToListMessages(listOf(newMessage), reactions, currentUserId))
        }
    }

    fun mergeMessages(
        oldMessages: List<ListMessage>,
        newMessages: List<Message>,
        reactions: List<Reaction>,
        currentUserId: Int
    ): List<ListMessage> {
        return oldMessages.toMutableList().apply {
            removeAt(oldMessages.lastIndex)
            addAll(mapToListMessages(newMessages, reactions, currentUserId))
        }
    }

    fun mapToListMessages(
        messages: List<Message>,
        emojis: List<Reaction>,
        currentUserId: Int
    ): List<ListMessage> {
        val listMessages = mutableListOf<ListMessage>()
        messages.forEach { message ->
            val reactions = message.reactions

            val messageContent = mapToMessageContent(message.content, emojis)
            val listReactions = mapToListReactions(reactions, currentUserId)
            val messageType =
                if (message.senderId == currentUserId) MessageType.OUTCOME else MessageType.INCOME

            listMessages.add(
                ListMessage(
                    id = message.id,
                    senderName = message.senderName,
                    content = messageContent,
                    senderAvatarUrl = message.senderAvatarUrl,
                    date = message.date,
                    time = DateUtils.getTime(message.date),
                    reactions = listReactions,
                    messageType = messageType
                )
            )
        }
        return listMessages
    }

    private fun mapToMessageContent(content: String, emojis: List<Reaction>): SpannableString {
        return SpannableString(
            HtmlCompat.fromHtml(
                if (content.contains(":")) {
                    var isFirstColon = false
                    content.split(":").joinToString("") {
                        val emoji = emojis.find { emoji -> emoji.name == it }
                        val text = if (isFirstColon) ":$it" else it
                        isFirstColon = emoji?.emoji == null
                        emoji?.emoji ?: text
                    }
                } else {
                    content
                },
                HtmlCompat.FROM_HTML_MODE_COMPACT
            ).trim()
        )
    }

    private fun mapToListReactions(
        reactions: List<Reaction>,
        currentUserId: Int
    ): List<ListMessageReaction> {
        val userReactions = reactions.filter { it.userId == currentUserId }.map { it.emoji }
        val listReactions = mutableListOf<ListMessageReaction>()
        reactions.distinctBy { it.emoji }.forEach { reaction ->
            listReactions.add(
                ListMessageReaction(
                    name = reaction.name,
                    code = reaction.code,
                    type = reaction.type,
                    emoji = reaction.emoji,
                    count = reactions.count { it.emoji == reaction.emoji },
                    isSelected = userReactions.contains(reaction.emoji)
                )
            )
        }
        return listReactions.sortedByDescending { it.count }
    }

    fun mapToListItems(messages: List<ListMessage>): List<DelegateItem> {
        val listItems = mutableListOf<DelegateItem>()
        messages.forEachIndexed { i, message ->
            listItems.add(message)

            if (i == messages.lastIndex || DateUtils.notSameDay(message.date, messages[i + 1].date)) {
                listItems.add(ListDate(listItems.size, DateUtils.getDayDate(message.date)))
            }
        }

        return listItems
    }

}