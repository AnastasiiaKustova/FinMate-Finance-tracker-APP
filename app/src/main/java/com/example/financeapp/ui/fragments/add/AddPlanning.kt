package com.example.financeapp.ui.fragments.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.R
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.ui.fragments.CategoriesSelector
import com.example.financeapp.databinding.FragmentAddPlanningBinding
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.PlanningViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.data.model.PlanningClass
import com.example.financeapp.ui.common.DateRangePickerHelper
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.CategoryUiMapper
import com.example.financeapp.utils.Constance.KEY_CATEGORY
import com.example.financeapp.utils.Constance.MORE_TAG
import com.example.financeapp.utils.Constance.REQUEST_KEY_CATEGORY
import com.example.financeapp.utils.Constance.REQUEST_KEY_NEW_CATEGORY
import com.example.financeapp.ui.common.CurrencyManager
import com.example.financeapp.utils.Constance.KEY_CATEGORY_UUID
import com.example.financeapp.utils.MoneyTextWatcher
import com.example.financeapp.utils.StringNames
import com.example.financeapp.utils.getParcelableCompat
import com.example.financeapp.viewModel.factory.ViewModelType

class AddPlanning : Fragment() {

    lateinit var binding: FragmentAddPlanningBinding

    private var selectedCategory: CategoryClass? = null
    private lateinit var watcher: MoneyTextWatcher
    private lateinit var adapter: ItemsCategoryAdapter

    private val selectedType = Constance.expense
    private lateinit var  dateStartText : String
    private lateinit var  dateEndText : String
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private var newCategory: CategoryClass? = null

    private lateinit var planningViewModel: PlanningViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var stringNames : StringNames

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        initListeners()
        setStartDate()

        initAdapter()
        setCategory()

        binding.textCurrency.text = CurrencyManager.getCurrency()
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

    private fun initListeners() {
        with(binding) {

            watcher = MoneyTextWatcher(moneyText)
            moneyText.addTextChangedListener(watcher)

            editDateText.setOnClickListener { showDateRangePicker() }
            icCalendar.setOnClickListener { showDateRangePicker() }
            addButton.setOnClickListener { saveAction() }
            backButton.setOnClickListener { parentFragmentManager.popBackStack() }
        }
    }

    private fun setStartDate(){
        dateStartText = DateUtils.getCurrentDateStart()
        dateEndText = DateUtils.getPlanningDateEnd()

        binding.editDateText.text = DateUtils.dateForLabel(dateStartText, dateEndText)
    }

    private fun initAdapter(){

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_NEW_CATEGORY,  // тот же ключ
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

    private fun setCategory(){

        categoryViewModel.categories.observe(viewLifecycleOwner){ categories->
            updateCategories(categories)
        }

        categoryViewModel.loadData(Query.defaultForCategory(selectedType))
    }

    private fun showDateRangePicker() {
        DateRangePickerHelper(parentFragmentManager, startDateMillis, endDateMillis) { start, end, startMillis, endMillis ->
            startDateMillis = startMillis
            endDateMillis = endMillis
            dateStartText = start
            dateEndText = end
            binding.editDateText.text = DateUtils.dateForLabel(dateStartText, dateEndText)
        }.show()
    }

    private fun canSave() : Boolean{

        val money = watcher.getValue()

        return when {
            money <= 0 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_MONEY); false
            }
            selectedCategory == null -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_CATEGORY); false
            }

            dateStartText.isBlank() || dateEndText.isBlank() ->{
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_DATE); false
            }

            else -> true
        }
    }

    private fun saveAction(){
        if (!canSave()) return

        val moneyInt = watcher.getValue()
        val comment = binding.comment.text.toString()

        val newPlanning = PlanningClass(
            categoryUUID = selectedCategory?.UUID!!,
            planningValue = moneyInt,
            dateStart = dateStartText,
            dateEnd = dateEndText,
            comment = comment
        )

        planningViewModel.addPlanning(newPlanning)
        parentFragmentManager.popBackStack()
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