package ru.cashbox.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import ru.cashbox.android.ui.base.ActivityBase
import ru.cashbox.android.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.ui.base.FragmentBase

class ActivityMain : ActivityBase<ViewModelMain, ActivityMainBinding>() {

    override val viewModel: ViewModelMain by viewModel()
    override fun getLayoutResId() = R.layout.activity_main

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onActivityCreate()
        startService(Intent(this, StateService::class.java))
    }

    override fun onDestroy() {
        viewModel.onActivityDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        val fragment = (supportFragmentManager.findFragmentById(R.id.fragment_navigation_host) as NavHostFragment).childFragmentManager.fragments[0]

        if (fragment is FragmentBase<*, *> && fragment.onBackPressed()) {
            return
        } else if (viewModel.isLoadingVisible.value == true) {
            viewModel.isLoadingVisible.value = false
        } else {
            super.onBackPressed()
        }
    }
}