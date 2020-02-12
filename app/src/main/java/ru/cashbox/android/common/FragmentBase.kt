package ru.cashbox.android.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import ru.cashbox.android.model.EventType
import ru.cashbox.android.refactor.ActivityMain

abstract class FragmentBase<VM : ViewModelBase, B : ViewDataBinding> : Fragment() {

    protected lateinit var binding: B
    abstract val viewModel: VM
    protected lateinit var navigationController: NavController

    @LayoutRes
    abstract fun getLayoutResId() : Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutResId(), null, false)

        configureBinding()
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationController = Navigation.findNavController(view)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onViewResume()
    }

    protected open fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.LOADING_SHOW -> viewModel.onShowLoadingAction()
            EventType.LOADING_HIDE -> viewModel.onHideLoadingAction()
            else -> {
                if (activity is ActivityMain) {
                    (activity as ActivityMain).onNavigationEvent(type, args)
                }
            }
        }
    }

    protected fun showDialog(dialog: DialogFragment) {
        if (isResumed) {
            dialog.show(this.childFragmentManager, dialog.javaClass.simpleName)
        }
    }

    protected fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @CallSuper
    protected open fun configureBinding() {
        viewModel.navigateEvent.observe(viewLifecycleOwner, Observer {
            it.getTypeIfNotHandled()?.let {type ->
                onNavigationEvent(type, it?.args)
            }
        })
    }
}