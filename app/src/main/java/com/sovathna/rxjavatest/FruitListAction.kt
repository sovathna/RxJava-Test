package com.sovathna.rxjavatest

sealed class FruitListAction {
    data class LoadFruit(val filter: String = "all") : FruitListAction()
    data class AddFavorite(val name: String, val filter: String) : FruitListAction()
    data class DeleteFavorite(val name: String, val filter: String) : FruitListAction()
}