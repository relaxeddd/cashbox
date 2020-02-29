package ru.cashbox.android.ui

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.cashbox.android.model.LogAppState
import ru.cashbox.android.model.repositories.RepositoryUsers

class StateService : Service() {

    companion object {
        var repositoryUsers: RepositoryUsers? = null
    }

    private val ioScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        ioScope.launch {
            repositoryUsers?.logAppState(LogAppState.CLOSE_APP)
            stopSelf()
        }
    }
}