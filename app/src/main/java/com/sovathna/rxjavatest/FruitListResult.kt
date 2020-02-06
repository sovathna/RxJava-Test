package com.sovathna.rxjavatest

sealed class FruitListResult {
    object Progress : FruitListResult()
    data class Success(val fruits: List<Fruit>, val filter: String) : FruitListResult()
    data class Error(val throwable: Throwable) : FruitListResult()

    object FavoriteProgress : FruitListResult()

}