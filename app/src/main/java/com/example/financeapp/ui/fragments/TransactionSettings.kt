package com.example.financeapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.R
import com.example.financeapp.data.model.SettingsItemClass

import com.example.financeapp.utils.Constance
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.SettingsViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.databinding.FragmentTransactionSettingsBinding
import com.example.financeapp.ui.adapter.ItemsWithDateAdapter
import com.example.financeapp.ui.adapter.SettingsAdapter
import com.example.financeapp.ui.common.SpinnerUtils
import com.example.financeapp.ui.fragments.info.TransactionInfo
import com.example.financeapp.utils.Constance.INFO_TAG
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.factory.ViewModelType
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TransactionSettings : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTransactionSettingsBinding

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private lateinit var currentSettings: Settings

    private lateinit var adapterTypes: SettingsAdapter
    private lateinit var adapterCategories: SettingsAdapter
    private lateinit var stringNames: StringNames

    private var selectedTypes = listOf<SettingsItemClass>()
    private var selectedCategories = listOf<SettingsItemClass>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        initAdapter()
        setSettings()
        setCategory()

        binding.saveBtn.setOnClickListener {
            selectedTypes = adapterTypes.getSelectedItems()
            selectedCategories = adapterCategories.getSelectedItems()

            val newSettings = settingsViewModel.settings.value!!.copy(
                operationTypes = selectedTypes.map { it.code },
                categoryIDs = selectedCategories
            )
            settingsViewModel.updateSettings(newSettings)
            dismiss()
        }

        binding.resetBtn.setOnClickListener {
            adapterCategories.resetSelectedItems()
            adapterTypes.resetSelectedItems()
        }
    }

    private fun setupViewModels() {
        categoryViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CATEGORY
        )

        settingsViewModel = ViewModelProvider(requireActivity())
            .get(SettingsViewModel::class.java)
    }

    private fun setSettings() {
        currentSettings = settingsViewModel.settings.value ?: Settings.default()
        settingsViewModel.initIfEmpty(currentSettings)

        if (currentSettings.operationTypes != null) {
            val types = currentSettings.operationTypes!!

            val selectedTypes = mutableListOf<SettingsItemClass>()

            for (type in types) {
                selectedTypes.add(
                    SettingsItemClass(
                        "0",
                        when (type) {
                            Constance.admission -> stringNames.ADMISSION_OP
                            Constance.expense -> stringNames.EXPENSE_OP
                            Constance.minus -> stringNames.TRANSFERS
                            else -> continue
                        },
                        type
                    )
                )
            }

            adapterTypes.updateSelectedItems(selectedTypes)
        }

        if (currentSettings.categoryIDs != null) {
            val categories = currentSettings.categoryIDs!!

            adapterCategories.updateSelectedItems(categories)
        }
    }

    private fun setCategory() {
        categoryViewModel.getAllCategories()

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->

            if (categories.isEmpty())
                binding.categoriesTitle.visibility = View.GONE
            else
                binding.categoriesTitle.visibility = View.VISIBLE

            adapterCategories.updateItems(categories.map {
                SettingsItemClass(
                    it.id.toString(),
                    it.title,
                    it.title
                )
            })
        }
    }

    private fun initAdapter() {
        with(binding) {

            val typesList =
                arrayListOf(
                    SettingsItemClass("0", stringNames.ADMISSION_OP, Constance.admission),
                    SettingsItemClass("0", stringNames.EXPENSE_OP, Constance.expense),
                    SettingsItemClass("0", stringNames.TRANSFERS, Constance.minus)
                )

            val layoutManagerTypes = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW          // элементы идут в строку
                flexWrap = FlexWrap.WRAP                   // перенос на новую строку
                justifyContent = JustifyContent.FLEX_START // выравнивание слева
            }

            adapterTypes = SettingsAdapter(typesList)
            types.layoutManager = layoutManagerTypes
            types.adapter = adapterTypes

            adapterTypes.onItemClick = { item ->
                selectedTypes = adapterTypes.getSelectedItems()
            }

            val layoutManagerCategories = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW          // элементы идут в строку
                flexWrap = FlexWrap.WRAP                   // перенос на новую строку
                justifyContent = JustifyContent.FLEX_START // выравнивание слева
            }

            adapterCategories = SettingsAdapter(emptyList())
            categories.layoutManager = layoutManagerCategories
            categories.adapter = adapterCategories

            adapterCategories.onItemClick = { item ->
                selectedCategories = adapterCategories.getSelectedItems()
            }
        }
    }
}