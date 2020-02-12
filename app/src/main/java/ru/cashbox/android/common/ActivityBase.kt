package ru.cashbox.android.common

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ru.cashbox.android.model.EventType
import kotlin.system.exitProcess

abstract class ActivityBase<VM : ViewModelBase, B : ViewDataBinding> : AppCompatActivity(), LifecycleOwner {

    private var listenerHomeMenuButton: () -> Unit = {}
    protected lateinit var binding: B
    abstract val viewModel: VM
    var isActivityResumed = false
        private set

    @LayoutRes
    abstract fun getLayoutResId() : Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, getLayoutResId())
        configureBinding()
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
        viewModel.onViewResume()
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            listenerHomeMenuButton.invoke()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    open fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.PRESS_BACK -> onBackPressed()
            EventType.LOADING_SHOW -> viewModel.onShowLoadingAction()
            EventType.LOADING_HIDE -> viewModel.onHideLoadingAction()
            EventType.EXIT -> {
                finishAffinity()
                exitProcess(0)
            }
        }
    }

    fun configureMenu(isShowHomeMenuButton: Boolean = false, homeMenuButtonIconResId: Int, clickListener: () -> Unit, elevation: Float) {
        supportActionBar?.setDisplayHomeAsUpEnabled(isShowHomeMenuButton)
        supportActionBar?.setHomeAsUpIndicator(homeMenuButtonIconResId)
        supportActionBar?.elevation = elevation
        listenerHomeMenuButton = clickListener
    }

    @CallSuper
    protected open fun configureBinding() {
        viewModel.navigateEvent.observe(this, Observer {
            it.getTypeIfNotHandled()?.let {type ->
                onNavigationEvent(type, it?.args)
            }
        })
    }

    protected fun showDialog(dialog: DialogFragment) {
        if (isActivityResumed) {
            dialog.show(supportFragmentManager, dialog.javaClass.simpleName)
        }
    }

    protected fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}