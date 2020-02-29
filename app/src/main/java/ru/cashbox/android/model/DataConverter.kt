package ru.cashbox.android.model

fun convertResponseCategory(responseCategory: ResponseCategory) : Category {
    return Category(responseCategory.id, responseCategory.name, responseCategory.imageUrl, responseCategory.parentId,
        responseCategory.type, responseCategory.defaultCategory)
}

fun convertResponseTechMap(techMap: ResponseTechMap) : TechMap {
    return TechMap(techMap.id, techMap.name, techMap.imageUrl, techMap.category.id,
        techMap.price, techMap.costPrice, techMap.weight, convertResponseTechMapGroups(techMap.modificatorGroups, techMap.id))
}

fun convertResponseIngredient(ingredient: ResponseIngredient, categoryId: Long) : Ingredient {
    return Ingredient(ingredient.id, ingredient.name, ingredient.imageUrl, categoryId, ingredient.type,
        ingredient.price, ingredient.costPrice, ingredient.unit)
}

fun convertResponseTechMapGroups(list: List<ResponseTechMapGroup>, techmapId: Long) : List<TechMapGroup> {
    val answer = ArrayList<TechMapGroup>()
    list.forEach { answer.add(TechMapGroup(it.id, it.name, it.modificatorsCount, techmapId, convertResponseModificators(it.modificators, it.id, it.name, it.modificatorsCount))) }
    return answer
}

fun convertResponseModificators(list: List<ResponseTechMapModificator>, groupId: Long, groupName: String, maxCount: Int) : List<TechMapModificator> {
    val answer = ArrayList<TechMapModificator>()
    list.forEach { answer.add(TechMapModificator(it.id, it.imageUrl, it.name, groupId, groupName, it.count, maxCount)) }
    return answer
}