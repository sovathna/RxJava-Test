package com.sovathna.rxjavatest

data class FruitListViewState(
    val initialize: Boolean = true,
    val progress: Boolean = false,
    val error: String? = null,
    val fruits: List<Fruit> = arrayListOf(),
    val filter: String = "all"
)