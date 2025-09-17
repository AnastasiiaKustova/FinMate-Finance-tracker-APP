package com.example.financeapp.ui.fragments.statistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.utils.Constance
import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.dbManager.OperationTypes
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.SettingsViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.ui.adapter.StatisticExpenseAdapter
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.SettingsItemClass
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.useCase.settings.SettingsQueryBuilder
import com.example.financeapp.databinding.FragmentStatisticExpenseBinding
import com.example.financeapp.ui.activity.MainActivity
import com.example.financeapp.ui.common.DateRangePickerHelper
import com.example.financeapp.ui.common.PieChartHelper
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.PlanningViewModel
import com.example.financeapp.viewModel.factory.ViewModelType

class StatisticExpense : Fragment() {

    private lateinit var binding: FragmentStatisticExpenseBinding

    private lateinit var operationViewModel: OperationViewModel
    private lateinit var planningViewModel: PlanningViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private lateinit var adapter: StatisticExpenseAdapter
    private lateinit var stringNames: StringNames

    private var dateStartText: String = ""
    private var dateEndText: String = ""
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private var money: Int = 0
    private var query = Query.empty()
    private var categories = emptyList<CategoryClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        setDate()
        initAdapter()
        updateSettings()

        binding.datePeriod.setOnClickListener {
            showDateRangePicker()
        }

        PieChartHelper.setup(requireContext(), binding.pieChart, binding.radialOverlay, binding.centerIcon, binding.centerText, categories, emptyList(), money)

        PieChartHelper.setOnCategorySelectedListener { selectedCategory ->
            if (selectedCategory != null) {
                adapter.updateSelectedCategoryUUID(selectedCategory.UUID)
            } else {
                adapter.updateSelectedCategoryUUID("")

            }
        }
    }

    private fun initAdapter(){

        adapter = StatisticExpenseAdapter(emptyList(), emptyList(), emptyList(),0)
        binding.categoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryList.adapter = adapter

        adapter.onItemClick = {item ->
            settingsViewModel.updateSettings(
                Settings.default().copy(
                    dateStart = dateStartText,
                    dateEnd = dateEndText,
                    categoryIDs = arrayListOf(SettingsItemClass(
                        UUID = item.info.categoryUUID ?: "",
                        title = item.info.categoryName ?: stringNames.NOT_FOUND,
                        code = item.info.categoryName ?: stringNames.NOT_FOUND
                        )
                    )
                )
            )
            (activity as? MainActivity)?.openHomeThenAllTransactions()
        }
    }

    private fun setupViewModels(){
        settingsViewModel = ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)

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

        planningViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.PLANNING
        )

        setSettings()
        setOperations()
        setCategories()
        setPlanning()
    }

    private fun setSettings(){
        updateSettings()

        settingsViewModel.settings.observe(viewLifecycleOwner) {settings ->
            query = SettingsQueryBuilder(settings, requireContext()).build()
            money = operationViewModel.getBalance(query, false)
            operationViewModel.loadDataWithQuery(query, OperationTypes.CATEGORY)
            planningViewModel.loadData(Query.defaultForPlanning(dateStartText, dateEndText))
        }
    }

    private fun setOperations(){
        operationViewModel.operations.observe(viewLifecycleOwner){operations ->

            adapter.updateItems(operations, money)

            if (operations.isNotEmpty()) {
                binding.noTransactionInfo.visibility = View.GONE
                binding.pieChartLayout.visibility = View.VISIBLE
            } else {
                binding.noTransactionInfo.visibility = View.VISIBLE
                binding.pieChartLayout.visibility = View.GONE
            }

            PieChartHelper.updateDataList(requireContext(), operations, money)
        }
    }

    private fun setCategories(){
        categoryViewModel.loadData(Query.empty())

        categoryViewModel.categories.observe(viewLifecycleOwner){categories ->
            this.categories = categories
            adapter.updateCategories(categories)

            PieChartHelper.updateCategories(requireContext(), categories)
        }
    }

    private fun setPlanning(){
        planningViewModel.planning.observe(viewLifecycleOwner){plannings ->
            adapter.updatePlaning(plannings)
        }
        planningViewModel.loadData(Query.defaultForPlanning(dateStartText, dateEndText))
    }

    private fun updateSettings() {
        settingsViewModel.updateSettings(
            Settings(
                dateStart = dateStartText,
                dateEnd = dateEndText,
                operationTypes = arrayListOf(Constance.expense),
                orderBy = "${DbTableOperation.COLUMN_NAME_MONEY} DESC"
            )
        )
    }

    private fun setDate(dateStart: String? = null, dateEnd: String? = null, startMillis: Long? = null, endMillis: Long? = null) {
        dateStartText = dateStart ?: DateUtils.getCurrentDateStart()
        dateEndText = dateEnd ?: DateUtils.getPlanningDateEnd()

        startDateMillis = startMillis ?: DateUtils.parseDateToMillis(dateStartText)
        endDateMillis = endMillis ?: DateUtils.parseDateToMillis(dateEndText)

        binding.datePeriod.text = DateUtils.dateForLabel(dateStartText, dateEndText)
    }

    private fun showDateRangePicker() {
        DateRangePickerHelper(parentFragmentManager, startDateMillis, endDateMillis) { start, end, startMillis, endMillis ->
            setDate(start, end, startMillis, endMillis)
            updateSettings()
        }.show()
    }
}