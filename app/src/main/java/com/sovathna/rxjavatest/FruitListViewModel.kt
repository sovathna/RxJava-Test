package com.sovathna.rxjavatest

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class FruitListViewModel : ViewModel() {

    private val fruits = mutableListOf<Fruit>().apply {
        Data.fruits.forEach { name ->
            add(Fruit(name))
        }
    }

    private val actionsSubject = PublishSubject.create<FruitListAction>()

    private val reducer =
        BiFunction<FruitListViewState, FruitListResult, FruitListViewState> { prev, result ->
            when (result) {
                is FruitListResult.Progress -> {
                    prev.copy(
                        initialize = false,
                        progress = true,
                        error = null
                    )
                }
                is FruitListResult.Error -> {
                    prev.copy(
                        progress = false,
                        error = result.throwable.message ?: "An error has occurred"
                    )
                }
                is FruitListResult.Success -> {
                    prev.copy(
                        progress = false,
                        fruits = result.fruits,
                        filter = result.filter
                    )
                }
                is FruitListResult.FavoriteProgress -> {
                    prev
                }
            }
        }

    private val getFruits =
        ObservableTransformer<FruitListAction.LoadFruit, FruitListResult> {
            it.flatMap { action ->
                Single.just(fruits.toList())
                    .delay(200, TimeUnit.MILLISECONDS)
                    .map { fruits ->
                        FruitListResult.Success(if (action.filter == "favorite") fruits.filter { it.favorite } else fruits,
                            action.filter)
                    }
                    .toObservable()
                    .cast(FruitListResult::class.java)
                    .onErrorReturn { FruitListResult.Error(it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWith(FruitListResult.Progress)
            }
        }

    private val addFavorite =
        ObservableTransformer<FruitListAction.AddFavorite, FruitListResult> {
            it.flatMap { action ->
                Single.fromCallable {
                    val tmp = fruits.find { it.name == action.name }
                    val index = fruits.indexOf(tmp)
                    fruits[index] = tmp!!.copy(favorite = true)
                    fruits.toList()
                }
                    .map { fruits ->
                        FruitListResult.Success(if (action.filter == "favorite") fruits.filter { it.favorite } else fruits, action.filter)
                    }
                    .toObservable()
                    .cast(FruitListResult::class.java)
                    .onErrorReturn { FruitListResult.Error(it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWith(FruitListResult.FavoriteProgress)
            }
        }

    private val deleteFavorite =
        ObservableTransformer<FruitListAction.DeleteFavorite, FruitListResult> {
            it.flatMap { action ->
                Single.fromCallable {
                    val tmp = fruits.find { it.name == action.name }
                    val index = fruits.indexOf(tmp)
                    fruits[index] = tmp!!.copy(favorite = false)
                    fruits.toList()
                }
                    .map { fruits ->
                        FruitListResult.Success(if (action.filter == "favorite") fruits.filter { it.favorite } else fruits, action.filter)
                    }
                    .toObservable()
                    .cast(FruitListResult::class.java)
                    .onErrorReturn { FruitListResult.Error(it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWith(FruitListResult.FavoriteProgress)
            }
        }

    private val actionsProcessor =
        ObservableTransformer<FruitListAction, FruitListResult> { actions ->
            actions.publish { selector ->
                Observable.merge(
                    selector.ofType(FruitListAction.LoadFruit::class.java).compose(
                        getFruits
                    ),
                    selector.ofType(FruitListAction.AddFavorite::class.java).compose(
                        addFavorite
                    ),
                    selector.ofType(FruitListAction.DeleteFavorite::class.java).compose(
                        deleteFavorite
                    )
                )
            }
        }

    val stateLiveData: LiveData<FruitListViewState> =
        LiveDataReactiveStreams.fromPublisher(
            actionsSubject.compose(actionsProcessor)
                .scan(FruitListViewState(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0)
                .toFlowable(BackpressureStrategy.BUFFER)
        )

    fun initialize(actions: Observable<FruitListAction>) {
        actions.subscribe(actionsSubject)
    }


}