package com.sovathna.rxjavatest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_fruit_list.*
import java.util.concurrent.TimeUnit

class FruitListFragment : Fragment() {

    private lateinit var adapter: FruitListAdapter
    private lateinit var viewModel: FruitListViewModel
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val loadAction = PublishSubject.create<FruitListAction.LoadFruit>()
    private val addFavoriteAction = PublishSubject.create<FruitListAction.AddFavorite>()
    private val deleteFavoriteAction = PublishSubject.create<FruitListAction.DeleteFavorite>()

    private val actions = Observable.merge(loadAction, addFavoriteAction, deleteFavoriteAction)

    private val itemClickSubject = PublishSubject.create<Fruit>()
    private var itemClickDispose: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[FruitListViewModel::class.java]
        adapter = FruitListAdapter()
        layoutManager = LinearLayoutManager(requireContext())

        itemClickDispose = itemClickSubject
            .subscribeOn(Schedulers.io())
            .throttleFirst(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Toast.makeText(
                    requireContext(),
                    "click fruit: ${it.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        viewModel.initialize(actions)

        viewModel.stateLiveData.observe(requireActivity(), Observer { state ->
            Log.d("===", state.toString())
            renderState(state)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fruit_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv.layoutManager = layoutManager
        rv.adapter = adapter
    }

    override fun onDestroy() {
        itemClickDispose?.dispose()
        super.onDestroy()
    }

    private fun renderState(state: FruitListViewState) {
        if (state.initialize) {
            loadAction.onNext(FruitListAction.LoadFruit("all"))
        }

        if (!state.initialize && state.fruits.isEmpty() && state.error == null)
            tv_empty.visibility = View.VISIBLE
        else
            tv_empty.visibility = View.GONE

        adapter.submitList(state.fruits)

        when {
            state.progress -> {
                pb.visibility = View.VISIBLE
                btn_filter.visibility = View.GONE
            }
            else -> {
                pb.visibility = View.GONE
                btn_filter.visibility = View.VISIBLE
                btn_filter.text = state.filter
                btn_filter.setOnClickListener {
                    loadAction.onNext(FruitListAction.LoadFruit(if (state.filter == "all") "favorite" else "all"))
                }
            }
        }

        if (state.error == null) {
            btn_retry.visibility = View.GONE
        } else {
            btn_retry.visibility = View.VISIBLE
        }

        adapter.setItemClickListener { index, fruit ->
            itemClickSubject.onNext(fruit)
        }

        adapter.setFavoriteItemClickListener { index, fruit ->
            if (!fruit.favorite)
                addFavoriteAction.onNext(FruitListAction.AddFavorite(fruit.name, state.filter))
            else
                deleteFavoriteAction.onNext(
                    FruitListAction.DeleteFavorite(
                        fruit.name,
                        state.filter
                    )
                )
        }
    }

}