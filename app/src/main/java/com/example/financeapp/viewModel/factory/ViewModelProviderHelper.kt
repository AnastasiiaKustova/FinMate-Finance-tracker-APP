package com.example.financeapp.viewModel.factory

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.PlanningViewModel

object ViewModelProviderHelper {

    fun <T : ViewModel> getViewModel(
        activity: FragmentActivity,
        context: Context,
        type: ViewModelType
    ): T {
        val factory = when (type) {
            ViewModelType.TRANSACTION -> provideOperationViewModelFactory(context)
            ViewModelType.CARD -> provideCardsViewModelFactory(context)
            ViewModelType.CATEGORY -> provideCategoryViewModelFactory(context)
            ViewModelType.PLANNING -> providePlanningViewModelFactory(context)
        }

        val clazz = when (type) {
            ViewModelType.TRANSACTION -> OperationViewModel::class.java
            ViewModelType.CARD -> CardsViewModel::class.java
            ViewModelType.CATEGORY -> CategoryViewModel::class.java
            ViewModelType.PLANNING -> PlanningViewModel::class.java
        } as Class<T>

        return ViewModelProvider(activity, factory).get(clazz)
    }

    private fun provideCategoryViewModelFactory(context: Context): CategoryViewModelFactory {
        return CategoryViewModelFactory(context.applicationContext)
    }

    private fun provideCardsViewModelFactory(context: Context): CardsViewModelFactory {
        return CardsViewModelFactory(context.applicationContext)
    }

    private fun providePlanningViewModelFactory(context: Context): PlanningViewModelFactory {
        return PlanningViewModelFactory(context.applicationContext)
    }

    private fun provideOperationViewModelFactory(context: Context): OperationViewModelFactory {
        return OperationViewModelFactory(context.applicationContext)
    }
}