package com.example.financeapp.ui.fragments.info

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.R
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.PlanningClass
import com.example.financeapp.databinding.FragmentPlanningInfoBinding
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter
import com.example.financeapp.ui.common.DateRangePickerHelper
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.ui.fragments.CategoriesSelector
import com.example.financeapp.ui.fragments.add.AddOrEditCategory
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.utils.CategoryUiMapper
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.Constance.EDIT_TAG
import com.example.financeapp.utils.Constance.KEY_CATEGORY
import com.example.financeapp.utils.Constance.KEY_CATEGORY_UUID
import com.example.financeapp.utils.Constance.MORE_TAG
import com.example.financeapp.utils.Constance.REQUEST_KEY_CATEGORY
import com.example.financeapp.utils.Constance.REQUEST_KEY_NEW_CATEGORY
import com.example.financeapp.utils.MoneyFormatter
import com.example.financeapp.utils.MoneyTextWatcher
import com.example.financeapp.utils.StringNames
import com.example.financeapp.utils.getParcelableCompat
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.PlanningViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.viewModel.factory.ViewModelType

class PlanningInfo : Fragment() {

    private lateinit var binding: FragmentPlanningInfoBinding
    private lateinit var planning: PlanningClass
    private var sourceTag: String? = null

    private lateinit var planningViewModel: PlanningViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var watcher: MoneyTextWatcher

    private var selectedCategory: CategoryClass? = null
    private lateinit var adapter: ItemsCategoryAdapter
    private val selectedType = Constance.expense
    private lateinit var dateStartText: String
    private lateinit var dateEndText: String
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private var newCategory: CategoryClass? = null
    private lateinit var stringNames : StringNames


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanningInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        private const val ARG_PLANNING = "planning"
        private const val ARG_TAG = "tag"

        fun newInstance(planning: PlanningClass, tag: String): PlanningInfo {
            val fragment = PlanningInfo()
            val args = Bundle().apply {
                putParcelable(ARG_PLANNING, planning)
                putString(ARG_TAG, tag)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun showErrorAndExit() {
        ToastHelper.show(requireContext(), stringNames.ERROR_LOADING)
        parentFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModels()

        stringNames = StringNames(requireContext())

        arguments?.let {
            val planningArg = arguments?.getParcelableCompat<PlanningClass>(ARG_PLANNING)

            if (planningArg == null) {
                showErrorAndExit()
            } else {
                planning = planningArg

                val findCategory = categoryViewModel.findCategoryByUUID(planning.categoryUUID)
                if (findCategory == null)
                    showErrorAndExit()
                else
                    selectedCategory = findCategory

                setDate(planning.dateStart, planning.dateEnd)
            }
            sourceTag = it.getString(ARG_TAG)
        }

        watcher = MoneyTextWatcher(binding.balanceEditText)
        binding.balanceEditText.addTextChangedListener(watcher)

        if (sourceTag == EDIT_TAG)
            initEditLayout()
        else
            initInfoLayout()

        initAdapter()
        setCategory()
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

    private fun initInfoLayout() {

        with(binding) {

            editLayout.visibility = View.GONE
            infoLayout.visibility = View.VISIBLE

            title.text = planning.categoryName

            comment.text = planning.comment.ifBlank { stringNames.NO_COMMENT_TEXT}

            date.text = DateUtils.dateForLabel(planning.dateStart, planning.dateEnd)

            balance.text = MoneyFormatter.format(planning.planningValue)
            spentValue.text = MoneyFormatter.format(planning.spentValue)
            remainderValue.text = MoneyFormatter.format(planning.planningValue - planning.spentValue)

            val progress = if (planning.planningValue > 0) {
                (planning.spentValue.toFloat() / planning.planningValue * 100).toInt()
            } else 0
            progressBar.progress = progress

            backBtn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            editOrDeleteBtn.setOnClickListener {
                sourceTag = EDIT_TAG
                initEditLayout()
            }
            deleteBtn.setOnClickListener {
                DeleteConfirmationDialog.show(requireContext()) {
                    planningViewModel.deletePlanning(planning)
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun initEditLayout() {
        with(binding) {
            editLayout.visibility = View.VISIBLE
            infoLayout.visibility = View.GONE

            title.text = stringNames.PLANNING_EDIT_TITLE
            editOrDeleteBtn.setImageResource(CategoryIcons.deleteIcon)

            balanceEditText.setText(planning.planningValue.toString())
            commentEditText.setText(planning.comment)

            editDateText.setOnClickListener { showDateRangePicker() }
            icCalendar.setOnClickListener { showDateRangePicker() }

            editOrDeleteBtn.setOnClickListener {
                DeleteConfirmationDialog.show(requireContext()) {
                    planningViewModel.deletePlanning(planning)
                    parentFragmentManager.popBackStack()
                }
            }

            saveBtn.setOnClickListener {
                save()
            }
        }

    }

    private fun initAdapter() {

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_NEW_CATEGORY,
            viewLifecycleOwner
        ) { _, bundle ->
            val categoryUUID = bundle.getString(KEY_CATEGORY_UUID) ?: ""
            newCategory = categoryViewModel.findCategoryByUUID(categoryUUID)
        }

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_CATEGORY, viewLifecycleOwner
        ) { _, bundle ->
            val selectedCategory =
                bundle.getParcelableCompat<CategoryClass>(KEY_CATEGORY)
            selectedCategory?.let {
                this.selectedCategory = it
                updateCategories(adapter.getCategories())
            }
        }

        adapter = ItemsCategoryAdapter(emptyList())
        binding.itemsCategory.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.itemsCategory.adapter = adapter

        adapter.onItemClick = { category, position ->

            when (category.title) {
                stringNames.ADD_TITLE -> {
                    val fragment =
                        AddOrEditCategory.newInstance(
                            null,
                            selectedType,
                            false
                        )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                stringNames.MORE_TITLE -> {
                    selectedType?.let {
                        val fragment = CategoriesSelector.newInstance(category, it, MORE_TAG)
                        fragment.show((activity as FragmentActivity).supportFragmentManager, null)
                    }
                }

                else -> {
                    selectedCategory = category
                    adapter.setSelectedPosition(position)
                }
            }
        }
    }

    private fun setCategory() {

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            updateCategories(categories)

            val category = categories.find { it.UUID == planning.categoryUUID }
            category?.let {
                if (it.colorId.isNotEmpty())
                    binding.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(it.colorId)))
            }

        }

        val query = Query.defaultForCategory(selectedType)
        categoryViewModel.loadData(query)
    }

    private fun save() {
        if (!canSave()) return

        val moneyInt = watcher.getValue()
        val comment = binding.comment.text.toString()

        val newPlanning = planning.copy(
            dateStart = dateStartText,
            dateEnd = dateEndText,
            categoryUUID = selectedCategory?.UUID!!,
            categoryName = "",
            planningValue = moneyInt,
            comment = comment,
            spentValue = 0
        )

        planningViewModel.updatePlanning(newPlanning)
        parentFragmentManager.popBackStack()
    }

    private fun canSave(): Boolean {
        val money = watcher.getValue()

        return when {
            money <= 0 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_MONEY); false
            }

            selectedCategory == null -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_CATEGORY); false
            }

            dateStartText.isBlank() || dateEndText.isBlank() -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_DATE); false
            }

            else -> true
        }
    }

    private fun showDateRangePicker() {
        DateRangePickerHelper(
            parentFragmentManager,
            startDateMillis,
            endDateMillis
        ) { start, end, startMillis, endMillis ->
            setDate(start, end, startMillis, endMillis)
        }.show()
    }

    private fun setDate(
        dateStart: String,
        dateEnd: String,
        startMillis: Long? = null,
        endMillis: Long? = null
    ) {
        startDateMillis = startMillis ?: DateUtils.parseDateToMillis(dateStart)
        endDateMillis = endMillis ?: DateUtils.parseDateToMillis(dateEnd)

        dateStartText = dateStart
        dateEndText = dateEnd

        binding.editDateText.text = DateUtils.dateForLabel(dateStartText, dateEndText)
    }

    private fun updateCategories(categories: List<CategoryClass>){
        selectedCategory = CategoryUiMapper.updateCategories(
            context = requireContext(),
            adapter = adapter,
            selectedType = selectedType,
            selectedCategory = selectedCategory,
            newCategory = newCategory,
            categories = categories
        )
        newCategory = null
    }
}