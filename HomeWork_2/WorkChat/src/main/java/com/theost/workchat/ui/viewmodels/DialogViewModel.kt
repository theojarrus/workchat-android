package com.theost.workchat.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.theost.workchat.data.models.core.MessageDate
import com.theost.workchat.data.models.core.Reaction
import com.theost.workchat.data.repositories.ChannelsRepository
import com.theost.workchat.data.repositories.MessagesRepository
import com.theost.workchat.data.repositories.ReactionsRepository
import com.theost.workchat.data.repositories.TopicsRepository
import com.theost.workchat.utils.DateUtils

class DialogViewModel : ViewModel() {

    private val _allData = MutableLiveData<Pair<List<Any>, List<Reaction>>>()
    val allData: LiveData<Pair<List<Any>, List<Reaction>>> = _allData

    private val _dialogInfo = MutableLiveData<Pair<String, String>>()
    val dialogInfo: LiveData<Pair<String, String>> = _dialogInfo

    fun loadData(dialogId: Int) {
        TopicsRepository.getTopics().find { it.id == dialogId }?.let { topic ->
            ChannelsRepository.getChannels().find { it.id == topic.channelId }?.let { channel ->
                _dialogInfo.postValue(Pair(channel.name, topic.name))
            }
        }
        val items = mutableListOf<Any>()
        val messages = MessagesRepository.getMessages(dialogId).sortedBy { it.date }
        val reactions = mutableListOf<Reaction>()
        messages.forEach { reactions.addAll(ReactionsRepository.getReactions(it.id)) }
        for (i in messages.indices) {
            if (i == 0 || !DateUtils.isSameDay(messages[i].date, messages[i - 1].date)) {
                items.add(MessageDate(DateUtils.getDayDate(messages[i].date)))
            }
            items.add(messages[i])
        }
        _allData.postValue(Pair(items, reactions))
    }

}