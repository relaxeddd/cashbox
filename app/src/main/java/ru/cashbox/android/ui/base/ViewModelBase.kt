package ru.cashbox.android.ui.base

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.FORBIDDEN
import ru.cashbox.android.common.MESSAGE
import ru.cashbox.android.model.NavigationEvent

abstract class ViewModelBase(app: App) : AndroidViewModel(app) {

    val isLoadingVisible = MutableLiveData(false)
    val navigateEvent = MutableLiveData<NavigationEvent>()
    protected val uiScope = CoroutineScope(Dispatchers.Main)
    protected val ioScope = CoroutineScope(Dispatchers.IO)

    open fun onViewResume() {}

    protected fun showErrorIfIncorrect(response: Response<*>) {
        if (!response.isSuccessful) {
            val errorStr: String
            val serverStr = response.errorBody()?.string()

            if (serverStr?.isNotEmpty() == true) {
                val errorJson = JSONObject(serverStr)
                errorStr = errorJson.optString(MESSAGE)
            } else {
                errorStr = getErrorStringByCode(response.code())
            }

            showToast(errorStr)
        }
    }

    protected fun getString(@StringRes resId: Int, arg1: Any? = null, arg2: Any? = null) : String {
        return getApplication<App>().getString(resId, arg1, arg2)
    }

    protected fun getStringByResName(resName: String): String {
        val context = getApplication<App>()
        val packageName = context.packageName
        val resId = context.resources.getIdentifier(resName, "string", packageName)
        return if (resId != 0) getString(resId) else resName
    }

    fun showToast(string: String) {
        uiScope.launch {
            Toast.makeText(getApplication<App>(), string, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(@StringRes resId: Int) {
        uiScope.launch {
            Toast.makeText(getApplication<App>(), resId, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToastLong(@StringRes resId: Int) {
        uiScope.launch {
            Toast.makeText(getApplication<App>(), resId, Toast.LENGTH_LONG).show()
        }
    }

    fun onShowLoadingAction() {
        isLoadingVisible.value = true
    }

    fun onHideLoadingAction() {
        isLoadingVisible.value = false
    }

    private fun getErrorStringByCode(code: Int) : String {
        return when (code) {
            FORBIDDEN -> getString(R.string.error_forbidden)
            else -> getString(R.string.error_undefined)
        }
    }
}