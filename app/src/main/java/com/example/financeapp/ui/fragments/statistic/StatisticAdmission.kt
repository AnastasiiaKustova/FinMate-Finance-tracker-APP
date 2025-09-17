package com.example.financeapp.ui.fragments.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.data.dbManager.OperationTypes
import com.example.financeapp.utils.Constance
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.ui.adapter.StatisticAdmissionAdapter
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.databinding.FragmentStatisticAdmissionBinding
import com.example.financeapp.ui.activity.MainActivity
import com.example.financeapp.ui.common.BarChartClickHelper
import com.example.financeapp.ui.common.BarChartHelper
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.useCase.settings.SettingsQueryBuilder
import com.example.financeapp.utils.Months
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.SettingsViewModel
import com.example.financeapp.viewModel.factory.ViewModelType

class StatisticAdmission : Fragment() {

    private lateinit var binding: FragmentStatisticAdmissionBinding

    private lateinit var operationViewModel: OperationViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var cardViewModel: CardsViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var stringNames: StringNames

    private var dateStartText: String = ""
    private var dateEndText: String = ""

    private lateinit var adapter: StatisticAdmissionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticAdmissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        setStartDate()
        initAdapter()
        setCategory()
        setCard()
        loadDate()

    }

    private fun setupViewModels() {
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

        cardViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CARD
        )

        operationViewModel.operations.observe(viewLifecycleOwner) { operations ->

            binding.barChart.visibility = if (operations.isEmpty()) View.GONE else View.VISIBLE
            binding.noTransactionInfo.visibility =
                if (operations.isEmpty()) View.VISIBLE else View.GONE

            adapter.updateItems(operations)

            BarChartHelper().setup(binding.barChart, operations)
            initBarChar()
            operationViewModel.loadBalance()
        }

        operationViewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.balance.text = balance
        }
    }

    private fun initAdapter() {
        adapter = StatisticAdmissionAdapter(emptyList(), emptyList(), emptyList())
        binding.itemList.layoutManager = LinearLayoutManager(requireContext())
        binding.itemList.adapter = adapter

        adapter.onItemClick = { item ->

            val range = Months.getMonthRange(item.date, dateStartText)

            if (range != null) {
                settingsViewModel.updateSettings(
                    Settings.default().copy(
                        dateStart = range.first,
                        dateEnd = range.second,
                        operationTypes = listOf(Constance.admission)
                    )
                )
                (activity as? MainActivity)?.openHomeThenAllTransactions()
            }
        }
    }

    private fun initBarChar() {
        val xVal = DateUtils.monthNumber - 1

        binding.scrollView.post {
            binding.scrollView.scrollTo(xVal * 48, 0)
        }

        BarChartClickHelper(binding.barChart, 12) { index ->
            if (xVal < index)
                binding.itemList.smoothScrollToPosition(0)
            else
                binding.itemList.smoothScrollToPosition(xVal - index)
        }
    }

    private fun setCategory() {
        categoryViewModel.loadData(Query.empty())

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateCategories(categories)
        }
    }

    private fun setCard() {
        cardViewModel.loadData(Query.empty())

        cardViewModel.cards.observe(viewLifecycleOwner) { cards ->
            adapter.updateCards(cards)
        }
    }

    private fun loadDate() {

        val settings = Settings.default().copy(
            dateStart = dateStartText,
            dateEnd = dateEndText,
            operationTypes = arrayListOf(Constance.admission)
        )

        val query = SettingsQueryBuilder(settings, requireContext()).build()

        operationViewModel.loadDataWithQuery(query, OperationTypes.ADMISSION)
    }

    private fun setStartDate() {
        dateStartText = DateUtils.getDateStart()
        dateEndText = DateUtils.getDateEnd()
        binding.datePeriod.text = DateUtils.getYear(dateStartText).toString()

        binding.minus.setOnClickListener {
            var yearInt = binding.datePeriod.text.toString().toInt()
            yearInt--
            val yearRange = DateUtils.getYearRange(yearInt)
            binding.datePeriod.text = (yearInt).toString()
            dateStartText = yearRange.first
            dateEndText = yearRange.second
            loadDate()
        }

        binding.plus.setOnClickListener {
            var yearInt = binding.datePeriod.text.toString().toInt()
            yearInt++
            val yearRange = DateUtils.getYearRange(yearInt)
            binding.datePeriod.text = (yearInt).toString()
            dateStartText = yearRange.first
            dateEndText = yearRange.second
            loadDate()
        }
    }
}

