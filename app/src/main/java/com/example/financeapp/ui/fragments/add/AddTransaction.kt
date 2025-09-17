package com.example.financeapp.ui.fragments.add

import android.icu.util.ULocale.Category
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.R
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.dbManager.DbTableOperation
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.ui.fragments.CategoriesSelector
import com.example.financeapp.databinding.FragmentAddTransactionBinding
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.ui.activity.MainActivity
import com.example.financeapp.ui.common.DatePickerHelper
import com.example.financeapp.ui.common.SpinnerUtils
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.CategoryUiMapper
import com.example.financeapp.utils.Constance.KEY_CATEGORY
import com.example.financeapp.utils.Constance.KEY_CATEGORY_UUID
import com.example.financeapp.utils.Constance.MORE_TAG
import com.example.financeapp.utils.Constance.REQUEST_KEY_CATEGORY
import com.example.financeapp.utils.Constance.REQUEST_KEY_NEW_CATEGORY
import com.example.financeapp.ui.common.CurrencyManager
import com.example.financeapp.utils.MoneyTextWatcher
import com.example.financeapp.utils.StringNames
import com.example.financeapp.utils.getParcelableCompat
import com.example.financeapp.viewModel.factory.ViewModelType
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTransaction : Fragment() {

    private lateinit var binding: FragmentAddTransactionBinding

    private lateinit var operationViewModel: OperationViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var cardsViewModel: CardsViewModel

    private lateinit var adapter: ItemsCategoryAdapter
    private lateinit var watcher: MoneyTextWatcher

    private lateinit var stringNames : StringNames
    private var selectedCategory: CategoryClass? = null
    private var newCategory: CategoryClass? = null
    private var selectedType: String = Constance.expense
    private var transactionDate: String = ""

    private var selectedDateMillis: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        initAdapter()
        initListeners()
        changeOperationType()
        //setCategories()
        setCards()

        binding.textCurrency.text = CurrencyManager.getCurrency()
        binding.expense.setChecked(true)
        transactionDate = DateUtils.getCurrentDate()
        binding.editDateText.text = DateUtils.dateSQLtoString(transactionDate)

        categoryViewModel.categories.observe(viewLifecycleOwner){ categories->
            updateCategories(categories)
        }

        binding.operationSelector.post {
            selectedType = binding.operationSelector
                .findViewById<RadioButton>(binding.operationSelector.checkedRadioButtonId)
                ?.tag?.toString() ?: Constance.expense
            loadCategories()
        }
    }

    private fun initListeners() {
        with(binding) {

            watcher = MoneyTextWatcher(moneyText)
            moneyText.addTextChangedListener(watcher)

            expense.setOnClickListener { changeOperationType() }
            admission.setOnClickListener { changeOperationType() }
            editDateText.setOnClickListener { showDatePicker() }
            icCalendar.setOnClickListener { showDatePicker() }
            addButton.setOnClickListener { saveAction() }
            addTypeBtn.setOnClickListener {
                AddNewCard().show(
                    (activity as FragmentActivity).supportFragmentManager,
                    null
                )
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

    private fun setCards() {
        SpinnerUtils.setupSpinnerCards(requireContext(), binding.spinnerType, emptyList())

        cardsViewModel.cards.observe(viewLifecycleOwner) { cards ->
            if (cards.size == 0) {
                binding.spinnerType.visibility = View.GONE
                binding.noTypeInfo.visibility = View.VISIBLE
            } else {
                binding.spinnerType.visibility = View.VISIBLE
                binding.noTypeInfo.visibility = View.GONE
            }

            SpinnerUtils.setupSpinnerCards(requireContext(), binding.spinnerType, cards)
        }

        cardsViewModel.loadData(Query.defaultForCards())
    }

    private fun initAdapter() {

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_NEW_CATEGORY,  // тот же ключ
            viewLifecycleOwner
        ) { _, bundle ->
            val categoryUUID = bundle.getString(KEY_CATEGORY_UUID) ?: ""
            newCategory = categoryViewModel.findCategoryByUUID(categoryUUID)
            if (newCategory?.typeOperation != selectedType)
                changeOperationType(newCategory?.typeOperation)
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
    
    private fun loadCategories() {
        categoryViewModel.loadData(Query.defaultForCategory(selectedType))
    }

    private fun showDatePicker() {
        val pickerHelper = DatePickerHelper(
            context = requireContext(),
            selectedDateMillis = selectedDateMillis,
            onDateSelected = { year, month, day, millis ->
                selectedDateMillis = millis // сохраняем выбранную дату

                val cal = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                transactionDate = DateUtils.parseMillisToString(cal)
                binding.editDateText.text = DateUtils.dateSQLtoString(transactionDate)
            }
        )

        pickerHelper.show()
    }

    private fun canSave(): Boolean {

        var balance = 0

        val selectedCard = binding.spinnerType.selectedItem as? CardsClass

        if (selectedCard == null){
            ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_CARD)
            return false
        }

        if (selectedType == Constance.expense) {

            val query = Query(
                selection = "${DbTableOperation.COLUMN_NAME_CARD_UUID} = ?",
                selectionArgs = arrayOf(selectedCard!!.UUID),
                sortOrder = null
            )

            balance = operationViewModel.getBalance(query)
        }

        val editDateText = binding.editDateText.text.toString().trim()
        val moneyInt = watcher.getValue()


        return when {
            selectedCategory == null ->{
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_CATEGORY); false
            }

            moneyInt <= 0 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_MONEY); false
            }

            selectedType.isBlank() -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_TYPE); false
            }

            binding.operationSelector.checkedRadioButtonId == -1 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_CARD); false
            }

            editDateText.isBlank() -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_DATE); false
            }

            selectedType == Constance.expense && balance < moneyInt -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NOT_ENOUGH_MONEY); false
            }

            else -> true
        }
    }

    private fun saveAction() {

        if (!canSave()) return

        val moneyInt = watcher.getValue()
        val selectedCard = binding.spinnerType.selectedItem as CardsClass
        val comment = binding.comment.text.toString()

        val newTransaction = OperationClass(
            type = selectedType,
            categoryUUID = selectedCategory?.UUID ?: "",
            date = transactionDate,
            comment = comment,
            cardUUID = selectedCard.UUID,
            money = moneyInt
        )

        operationViewModel.addTransaction(newTransaction)

        (activity as? MainActivity)?.navigateTo(R.id.home)
    }

    private fun changeOperationType(newSelectType: String? = null) {

        if (newSelectType != null){
            if (newSelectType == Constance.expense)
                binding.expense.setChecked(true)
            else
                binding.admission.setChecked(true)
        }

        selectedType = binding.operationSelector
            .findViewById<RadioButton>(binding.operationSelector.checkedRadioButtonId)
            ?.tag?.toString() ?: Constance.expense

        if (selectedType == Constance.expense)
            binding.typeText.text = stringNames.CARD_FROM
        else
            binding.typeText.text = stringNames.CARD_TO

        loadCategories()
    }
}