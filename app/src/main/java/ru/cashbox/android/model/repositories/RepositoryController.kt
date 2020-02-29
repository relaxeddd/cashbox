package ru.cashbox.android.model.repositories

object RepositoryController {

    var repositoryBuys: RepositoryBuys? = null
    var repositoryGoods: RepositoryGoods? = null
    var repositoryCashsessions: RepositoryCashsessions? = null
    var repositoryChecks: RepositoryChecks? = null

    fun init(repositoryBuys: RepositoryBuys?, repositoryGoods: RepositoryGoods?,
             repositoryCashsessions: RepositoryCashsessions?, repositoryChecks: RepositoryChecks?) {
        this.repositoryBuys = repositoryBuys
        this.repositoryCashsessions = repositoryCashsessions
        this.repositoryChecks = repositoryChecks
        this.repositoryGoods = repositoryGoods
        repositoryCashsessions?.repositoryChecks = repositoryChecks
    }

    fun onUnauthorized() {
        repositoryBuys?.clear()
        repositoryGoods?.clear()
        repositoryCashsessions?.clear()
        repositoryChecks?.clear()
    }
}