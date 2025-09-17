package com.example.financeapp.ui.fragments.allList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.R
import com.example.financeapp.ui.fragments.add.AddPlanning
import com.example.financeapp.ui.fragments.info.PlanningInfo
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.PlanningViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.ui.adapter.PlanningAdapter
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.databinding.FragmentPlanningBinding
import com.example.financeapp.ui.common.DateRangePickerHelper
import com.example.financeapp.utils.Constance.INFO_TAG
import com.example.financeapp.viewModel.factory.ViewModelType

class AllPlannings : Fragment() {

    private lateinit var binding: FragmentPlanningBinding
    private lateinit var planningViewModel: PlanningViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    private lateinit var adapter: PlanningAdapter

    private var dateStart: String = ""
    private var dateEnd: String = ""
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModels()
        initAdapter()

        binding.datePeriod.setOnClickListener {
            DateRangePickerHelper(
                parentFragmentManager,
                startDateMillis,
                endDateMillis
            ) { start, end, startMillis, endMillis ->
                startDateMillis = startMillis
                endDateMillis = endMillis
                loadDate(start, end)
            }.show()
        }

        binding.addPlanningBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AddPlanning())
                .addToBackStack(null)
                .commit()
        }

        dateStart = DateUtils.getCurrentDateStart()
        dateEnd = DateUtils.getPlanningDateEnd()

        setCategory()
        setPlanning()
    }

    private fun setupViewModels() {
        planningViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.PLANNING
        )

        categoryViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CATEGORY
        )

    }

    private fun initAdapter() {
        adapter = PlanningAdapter(emptyList(), emptyList())

        binding.itemList.layoutManager = LinearLayoutManager(requireContext())
        binding.itemList.adapter = adapter

        adapter.onItemClick = { planning ->
            val fragment = PlanningInfo.newInstance(planning, INFO_TAG)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setCategory() {
        categoryViewModel.loadData(Query.empty())

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateCategories(categories)
        }
    }

    private fun setPlanning() {
        planningViewModel.planning.observe(viewLifecycleOwner) { plannings ->
            with(binding) {
                //if (plannings.isNotEmpty() && (plannings[0].dateStart != dateStart || plannings[0].dateEnd != dateEnd)) return@observe

                noTransactionInfo.visibility =
                    if (plannings.isEmpty()) View.VISIBLE else View.GONE

                planningViewModel.loadPlanningWithSpent(
                    plannings,
                ) { updatedPlans ->
                    adapter.updateItems(updatedPlans)
                }
            }
        }

        loadDate(
            dateStart,
            dateEnd
        )
    }

    private fun loadDate(dateStart: String, dateEnd: String) {

        this.dateStart = dateStart
        this.dateEnd = dateEnd

        binding.datePeriod.text = DateUtils.dateForLabel(dateStart, dateEnd)

        planningViewModel.loadData(
            Query.defaultForPlanning(dateStart, dateEnd)
        )
    }
}