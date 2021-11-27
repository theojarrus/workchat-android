package com.theost.workchat.data.repositories

import com.theost.workchat.application.WorkChatApp
import com.theost.workchat.data.models.core.Reaction
import com.theost.workchat.data.models.state.CacheStatus
import com.theost.workchat.database.entities.mapToReaction
import com.theost.workchat.database.entities.mapToReactionEntity
import com.theost.workchat.network.api.RetrofitHelper
import com.theost.workchat.network.dto.mapToReactions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

object ReactionsRepository {

    private var cacheStatus: CacheStatus = CacheStatus.NOT_UPDATED

    private val service = RetrofitHelper.retrofitService

    fun getReactions(): Observable<Result<List<Reaction>>> {
        return if (cacheStatus == CacheStatus.UPDATED) {
            getReactionsFromCache().toObservable()
        } else {
            Observable.concat(
                getReactionsFromCache().toObservable(),
                getReactionsFromServer().toObservable()
            )
        }
    }

    private fun getReactionsFromServer(): Single<Result<List<Reaction>>> {
        return service.getReactions()
            .map { response -> Result.success(response.mapToReactions()) }
            .onErrorReturn { Result.failure(it) }
            .doOnSuccess { result ->
                if (result.isSuccess) {
                    val reactions = result.getOrNull()
                    if (reactions != null) addReactionsToDatabase(reactions)
                }
            }
            .subscribeOn(Schedulers.io())
    }

    private fun getReactionsFromCache(): Single<Result<List<Reaction>>> {
        return WorkChatApp.cacheDatabase.reactionsDao().getAll()
            .map { response -> Result.success(response.map { reactionEntity -> reactionEntity.mapToReaction() }) }
            .onErrorReturn { Result.failure(it) }
            .subscribeOn(Schedulers.io())
    }

    private fun addReactionsToDatabase(reactions: List<Reaction>) {
        WorkChatApp.cacheDatabase.reactionsDao()
            .insertAll(reactions.map { reaction -> reaction.mapToReactionEntity() })
            .doOnComplete { cacheStatus = CacheStatus.UPDATED }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun addReaction(messageId: Int, reactionName: String): Completable {
        return service.addReaction(
            messageId = messageId,
            emojiName = reactionName
        ).subscribeOn(Schedulers.io())
    }

    fun removeReaction(
        messageId: Int,
        reactionName: String,
        reactionCode: String,
        reactionType: String
    ): Completable {
        return service.removeReaction(
            messageId = messageId,
            emojiName = reactionName,
            emojiCode = reactionCode,
            reactionType = reactionType
        ).subscribeOn(Schedulers.io())
    }

}