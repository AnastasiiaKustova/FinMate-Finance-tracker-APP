package com.example.financeapp.ui.fragments.allList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.R
import com.example.financeapp.data.dbManager.OperationTypes
import com.example.financeapp.data.model.FilterChip
import com.example.financeapp.ui.adapter.ItemsWithDateAdapter
import com.example.financeapp.ui.fragments.info.TransactionInfo
import com.example.financeapp.ui.fragments.TransactionSettings
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.SettingsViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.useCase.settings.SettingsQueryBuilder
import com.example.financeapp.databinding.FragmentAllTransactionBinding
import com.example.financeapp.ui.activity.MainActivity
import com.example.financeapp.ui.common.DateRangePickerHelper
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.ui.common.FilterChipFactory
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.Constance.EDIT_TAG
import com.example.financeapp.utils.Constance.INFO_TAG
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.factory.ViewModelType

class AllTransaction : Fragment() {

    private lateinit var binding: FragmentAllTransactionBinding
    private lateinit var operationViewModel: OperationViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var cardsViewModel: CardsViewModel

    private lateinit var adapter: ItemsWithDateAdapter
    private lateinit var stringNames: StringNames

    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        initAdapter()

        binding.apply {

            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            settingsButton.setOnClickListener {
                TransactionSettings().show(parentFragmentManager, null)
            }

            datePeriod.setOnClickListener {
                DateRangePickerHelper(
                    parentFragmentManager,
                    startDateMillis,
                    endDateMillis
                ) { start, end, startMillis, endMillis ->
                    startDateMillis = startMillis
                    endDateMillis = endMillis

                    val updatedSettings = settingsViewModel.settings.value?.copy(
                        dateStart = start,
                        dateEnd = end
                    ) ?: Settings.default()

                    settingsViewModel.updateSettings(updatedSettings)
                }.show()
            }
        }

        setSettings()
        setOperation()
        setCategory()
        setCards()
    }

    private fun initAdapter() {

        adapter = ItemsWithDateAdapter(emptyList(), emptyList(), emptyList())

        binding.operationList.layoutManager = LinearLayoutManager(requireContext())
        binding.operationList.adapter = adapter

        adapter.onItemClick = { transaction ->
            val fragment = TransactionInfo.newInstance(transaction, INFO_TAG)
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.frame_layout,
                    fragment
                )
                .addToBackStack(null)
                .commit()
        }

        adapter.onDeleteClick = { item ->
            DeleteConfirmationDialog.show(requireContext()) {
                operationViewModel.deleteFromDb(item)
            }
        }

        adapter.onEditClick = { transaction ->
            when (transaction.type) {
                Constance.expense, Constance.admission -> {
                    val fragment = TransactionInfo.newInstance(transaction, EDIT_TAG)
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame_layout,
                            fragment
                        )
                        .addToBackStack(null)
                        .commit()
                }

                else -> Toast.makeText(
                    requireActivity(),
                    stringNames.ERROR_EDIT_MESSAGE,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun setupViewModels() {

        operationViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.TRANSACTION
        )

        categoryViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CATEGORY
        )

        cardsViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CARD
        )

        settingsViewModel = ViewModelProvider(requireActivity())
            .get(SettingsViewModel::class.java)

    }

    private fun setSettings() {

        // Инициализируем настройки, если они пустые
        if (settingsViewModel.settings.value == null) {
            settingsViewModel.initIfEmpty(Settings.default())
        }

        settingsViewModel.settings.observe(viewLifecycleOwner) { settings ->
            setDateLabel(settings)
            operationViewModel.loadDataWithQuery(
                SettingsQueryBuilder(settings, requireContext()).build(),
                OperationTypes.DATE
            )
            categoryViewModel.loadData(
                SettingsQueryBuilder(
                    settings,
                    requireContext()
                ).buildByCategory()
            )

            FilterChipFactory.addChips(
                binding.chipGroupTypes,
                FilterChipFactory.operationTypesToFilterChip(settings.operationTypes, stringNames),
                onRemove = { removedFilter ->
                    if (settings.operationTypes?.contains(removedFilter.tag) == true) {
                        val newSettings = settings.operationTypes.toMutableList()
                        newSettings.remove(removedFilter.tag)
                        settingsViewModel.updateSettings(
                            settings.copy(
                                operationTypes = newSettings
                            )
                        )
                    }
                },
                colorRes = R.color.White
            )

            FilterChipFactory.addChips(
                binding.chipGroupCategories,
                settings.categoryIDs?.map { FilterChip(title = it.title, tag = it.UUID) }
                    ?: emptyList(),
                onRemove = { removedFilter ->
                    if (settings.categoryIDs?.map { FilterChip(title = it.title, tag = it.UUID) }
                            ?.contains(removedFilter) == true) {
                        val newSettings = settings.categoryIDs.toMutableList()
                        val removedCategory = newSettings.find { it.UUID == removedFilter.tag }
                        if (removedCategory != null) {
                            newSettings.remove(removedCategory)
                            settingsViewModel.updateSettings(
                                settings.copy(
                                    categoryIDs = newSettings
                                )
                            )
                        }
                    }
                },
                colorRes = R.color.White
            )
        }
    }

    private fun setOperation() {
        operationViewModel.balance.observe(viewLifecycleOwner) { money ->
            binding.balance.text = money
        }

        operationViewModel.operations.observe(viewLifecycleOwner) { dataList ->
            binding.noTransactionInfo.visibility =
                if (dataList.isEmpty()) View.VISIBLE else View.GONE

            adapter.updateItems(dataList)

            operationViewModel.loadBalance()
        }
    }

    private fun setCategory() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateCategories(categories)
        }

        cardsViewModel.loadData()
    }

    private fun setCards() {

        cardsViewModel.cards.observe(viewLifecycleOwner) { cards ->
            adapter.updateCards(cards)
        }

        cardsViewModel.loadData(Query.defaultForCards())
    }

    private fun setDateLabel(settings: Settings) {
        binding.datePeriod.text = DateUtils.dateForLabel(settings.dateStart, settings.dateEnd)
    }
}