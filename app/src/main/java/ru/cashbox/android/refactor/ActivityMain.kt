package ru.cashbox.android.refactor

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import ru.cashbox.android.common.ActivityBase
import ru.cashbox.android.databinding.RefactorActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.model.EventType
import kotlin.system.exitProcess

class ActivityMain : ActivityBase<ViewModelMain, RefactorActivityMainBinding>() {

    companion object {
        const val EXTRA_NAVIGATION_ID = "extra.NAVIGATION_ID"

        const val REQUEST_SIGN_IN = 1312
        const val REQUEST_PLAY_SERVICES_RESULT = 7245

        private const val NAV_ID_NONE = -1
    }

    override val viewModel: ViewModelMain by viewModel()
    private lateinit var navigationController: NavController
    private var currentNavId = NAV_ID_NONE

    override fun getLayoutResId() = R.layout.refactor_activity_main

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigationController = Navigation.findNavController(this, R.id.fragment_navigation_host)
        navigationController.addOnDestinationChangedListener { _, destination, _ ->
            currentNavId = destination.id
        }

        if (savedInstanceState == null) {
            val initialNavId = intent.getIntExtra(EXTRA_NAVIGATION_ID, R.id.navigation_login_terminal)
            navigateTo(initialNavId)
        }

        viewModel.onViewCreate()
    }

    override fun onBackPressed() {
        if (viewModel.isShowLoading.value == true) {
            viewModel.isShowLoading.value = false
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.LOADING_SHOW -> viewModel.onShowLoadingAction()
            EventType.LOADING_HIDE -> viewModel.onHideLoadingAction()
            EventType.EXIT -> {
                finishAffinity()
                exitProcess(0)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }

    private fun navigateTo(navId: Int) {
        if (navId != currentNavId) {
            navigationController.navigate(navId)
        }
    }
}