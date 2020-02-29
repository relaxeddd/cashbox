package ru.cashbox.android.model.repositories

import androidx.lifecycle.MutableLiveData
import ru.cashbox.android.model.*
import ru.cashbox.android.model.http.ApiHelper

class RepositoryGoods(private val apiHelper: ApiHelper, repositoryUsers: RepositoryUsers) {

    private var isItemsAlreadyInit = false
    private val tokenEmployee = repositoryUsers.tokenEmployee
    val ingredients = MutableLiveData<List<Ingredient>>(ArrayList())
    val categories = MutableLiveData<List<Category>>(ArrayList())
    val techMaps = MutableLiveData<List<TechMap>>(ArrayList())
    val buyIngredients = MutableLiveData<List<Ingredient>>(ArrayList())

    fun clear() {
        isItemsAlreadyInit = false
    }

    suspend fun requestItems() : Boolean {
        if (isItemsAlreadyInit) {
            return true
        }

        val responseCategories = apiHelper.requestCategories(tokenEmployee.value)
        val responseTechMaps = apiHelper.requestTechMaps(tokenEmployee.value)
        val responseBuyIngredients = apiHelper.requestIngredients(tokenEmployee.value)
        val categoriesAnswer = responseCategories.body()
        val techMapsAnswer = responseTechMaps.body()
        val buyIngredientsAnswer = responseBuyIngredients.body()
        val parsedCategories = ArrayList<Category>()
        val parsedTechMaps = ArrayList<TechMap>()
        val parsedIngredients = ArrayList<Ingredient>()
        val parserBuyIngredients = ArrayList<Ingredient>()
        val isSuccessAnswer = responseCategories.isSuccessful && responseTechMaps.isSuccessful

        if (responseCategories.isSuccessful && categoriesAnswer != null) {
            categoriesAnswer.forEach {category ->
                parsedCategories.add(convertResponseCategory(category))
                category.ingredients.forEach {
                    val ingredient = convertResponseIngredient(it, category.id)
                    parsedIngredients.add(ingredient)
                    parserBuyIngredients.add(ingredient)
                }
            }
        }
        if (responseTechMaps.isSuccessful && techMapsAnswer != null) {
            techMapsAnswer.forEach { parsedTechMaps.add(convertResponseTechMap(it)) }
        }
        if (responseBuyIngredients.isSuccessful && buyIngredientsAnswer != null) {
            buyIngredientsAnswer.forEach { parserBuyIngredients.add(convertResponseIngredient(it, it.category?.id ?: 0)) }
        }
        ingredients.value = parsedIngredients
        categories.value = parsedCategories
        techMaps.value = parsedTechMaps
        buyIngredients.value = parserBuyIngredients

        if (isSuccessAnswer) {
            isItemsAlreadyInit = true
        }

        return isSuccessAnswer
    }
}