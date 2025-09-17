package com.example.financeapp.ui.fragments.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.databinding.FragmentTransactionInfoBinding
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter
import com.example.financeapp.ui.common.DatePickerHelper
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.ui.common.SpinnerUtils
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.ui.fragments.CategoriesSelector
import com.example.financeapp.ui.fragments.add.AddOrEditCategory
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.utils.CategoryUiMapper
import com.example.financeapp.utils.CommentUtils
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
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.viewModel.factory.ViewModelType
import java.util.Calendar

class TransactionInfo() : Fragment() {

    private lateinit var binding: FragmentTransactionInfoBinding
    private lateinit var transaction: OperationClass
    private var sourceTag: String? = null

    private lateinit var operationViewModel: OperationViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var cardsViewModel: CardsViewModel
    private lateinit var adapter: ItemsCategoryAdapter

    private lateinit var watcher: MoneyTextWatcher
    private var selectedCategory: CategoryClass? = null
    private lateinit var selectedType: String
    private lateinit var stringNames : StringNames
    private var selectedDateMillis: Long? = null
    private var newCategory: CategoryClass? = null
    private var transactionDate: String = ""

    private var onlyRead: Boolean = false

    companion object {
        private const val ARG_TRANSACTION = "transaction"
        private const val ARG_TAG = "tag"

        fun newInstance(transaction: OperationClass, tag: String): TransactionInfo {
            val fragment = TransactionInfo()
            val args = Bundle().apply {
                putParcelable(ARG_TRANSACTION, transaction)
                putString(ARG_TAG, tag)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun showErrorAndExit() {
        showToast(stringNames.ERROR_LOADING)
        parentFragmentManager.popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        stringNames = StringNames(requireContext())

        setupViewModels()
        initAdapter()

        arguments?.let {
            val transactionArg = it.getParcelable<OperationClass>(ARG_TRANSACTION)
            if (transactionArg == null) {
                showErrorAndExit()
            } else {
                transaction = transactionArg

                selectedType = transaction.type

                when (selectedType){
                    Constance.admission -> {
                        binding.operationSelector.check(binding.admission.id)
                        binding.cardText.text = stringNames.CARD_TO
                    }
                    Constance.expense -> {
                        binding.operationSelector.check(binding.expense.id)
                        binding.cardText.text = stringNames.CARD_FROM
                    }
                    else -> onlyRead = true
                }

                setDate(transaction.date)

                if (!onlyRead){
                    val findCategory = categoryViewModel.findCategoryByUUID(transaction.categoryUUID)
                    if (findCategory == null)
                        selectedCategory = null
                    else
                        selectedCategory = findCategory
                }
                else selectedCategory = null

            }
            sourceTag = it.getString(ARG_TAG)

            SpinnerUtils.setupSpinnerCards(requireContext(), binding.spinnerType, emptyList())
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        watcher = MoneyTextWatcher(binding.moneyEditText)
        binding.moneyEditText.addTextChangedListener(watcher)

        if (sourceTag == EDIT_TAG)
            initEditLayout()
        else
            initInfoLayout()
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

        setCategories()
        setCards()
    }

    private fun setCategories() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            updateCategories(categories)
        }
        loadCategories()
    }

    private fun setCards() {
        cardsViewModel.cards.observe(viewLifecycleOwner) { cards ->
            val cards_ = cards.filter { it.visible == 1 }
            if (cards_.size == 0) {
                binding.spinnerType.visibility = View.GONE
                binding.noTypeInfo.visibility = View.VISIBLE
            } else {
                binding.spinnerType.visibility = View.VISIBLE
                binding.noTypeInfo.visibility = View.GONE
            }

            SpinnerUtils.setupSpinnerCards(requireContext(), binding.spinnerType, cards_)
        }
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

                        fragment.show(
                            (activity as FragmentActivity).supportFragmentManager, null
                        )
                    }
                }
                else -> {
                    selectedCategory = category
                    adapter.setSelectedPosition(position)
                }
            }
        }
    }

    private fun initInfoLayout() {
        binding.apply {
            infoLayout.visibility = View.VISIBLE
            editLayout.visibility = View.GONE

            title.text = stringNames.INFO_TITLE

            card.text = transaction.cardName

            balance.text = MoneyFormatter.format(transaction.money)

            if (transaction.categoryName.isNotEmpty())
                category.text = transaction.categoryName
            else
                category.text = stringNames.NOT_FOUND

            type.text = when(transaction.type){
                Constance.minus, Constance.plus -> stringNames.TRANSFERS
                Constance.admission -> stringNames.ADMISSION_OP
                Constance.expense -> stringNames.EXPENSE_OP
                else -> transaction.type
            }

            when (transaction.type){
                Constance.minus ->{
                    categoryLayout.visibility = View.GONE
                    if (transaction.cardToName.isNotEmpty())
                    {
                        cardToLayout.visibility = View.VISIBLE
                        cardTo.text = transaction.cardToName
                    }
                    else
                        cardToLayout.visibility = View.GONE

                }
                else ->{
                    cardToLayout.visibility = View.GONE
                    categoryLayout.visibility = View.VISIBLE
                }
            }

            if (CommentUtils.getUserComment(transaction.comment).isNotEmpty())
                comment.text = transaction.comment
            else
                comment.text = stringNames.NO_COMMENT_TEXT

            if (onlyRead){
                editOrDeleteBtn.visibility = View.INVISIBLE
                category.text = "-"
            } else
                editOrDeleteBtn.visibility = View.VISIBLE

            editOrDeleteBtn.setOnClickListener {
                sourceTag = EDIT_TAG
                initEditLayout()
            }

            deleteBtn.setOnClickListener {
                DeleteConfirmationDialog.show(requireContext()) {
                    delete()
                }
            }
        }

    }

    private fun initEditLayout() {
        with(binding) {
            infoLayout.visibility = View.GONE
            editLayout.visibility = View.VISIBLE

            title.text = stringNames.TRANSACTION_EDIT_TITLE
            editOrDeleteBtn.setImageResource(CategoryIcons.deleteIcon)

            val adapter = spinnerType.adapter as ArrayAdapter<CardsClass>
            val position = (0 until adapter.count).firstOrNull { i ->
                adapter.getItem(i)?.UUID == transaction.cardUUID
            } ?: -1
            if (position >= 0) {
                spinnerType.setSelection(position)
            }

            moneyEditText.setText(transaction.money.toString())
            if (transaction.comment != stringNames.NO_COMMENT_TEXT)
                commentEditText.setText(transaction.comment)

            editOrDeleteBtn.setOnClickListener {
                DeleteConfirmationDialog.show(requireContext()) {
                    delete()
                }
            }

            saveButton.setOnClickListener {
                save()
            }

            editDateText.setOnClickListener { showDatePicker() }
            icCalendar.setOnClickListener { showDatePicker() }
            expense.setOnClickListener { changeOperationType() }
            admission.setOnClickListener { changeOperationType() }
        }
    }

    private fun delete(){
        if (transaction.type == Constance.minus) {
            val query = Query.queryForDeletedCard(transaction)
            if (query == null){
                showToast(stringNames.MESSAGE_ERROR_DELETE)
                return
            }
            val operations = operationViewModel.getTransactions(query)
            if (operations.isEmpty()){
                showToast(stringNames.MESSAGE_ERROR_DELETE)
                return
            }
            for (op in operations) {
                operationViewModel.deleteFromDb(op)
                break
            }
        }

        operationViewModel.deleteFromDb(transaction)
        parentFragmentManager.popBackStack()
    }

    private fun save() {
        if (!canSave()) return

        val moneyInt = watcher.getValue()
        val comment = binding.commentEditText.text.toString()
        val card = binding.spinnerType.selectedItem as CardsClass

        val newTransaction = transaction.copy(
            type = selectedType,
            categoryUUID = selectedCategory?.UUID!!,
            date = transactionDate,
            comment = comment,
            cardUUID = card.UUID,
            money = moneyInt
        )

        operationViewModel.updateOperation(newTransaction)
        parentFragmentManager.popBackStack()
    }

    private fun canSave(): Boolean {
        var balance = 0

        if (selectedType == Constance.expense) {
            val card = binding.spinnerType.selectedItem as CardsClass
            val query = Query.cardBalanceQuery(card.UUID)

            balance = operationViewModel.getBalance(query)
        }

        val editDateText = binding.editDateText.text.toString().trim()
        val money = watcher.getValue()

        return when {
            selectedCategory == null ->{
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_CATEGORY); false
            }

            money <= 0 -> {
                showToast(stringNames.MESSAGE_NO_MONEY); false
            }

            selectedType.isBlank() -> {
                showToast(stringNames.MESSAGE_NO_TYPE); false
            }

            binding.operationSelector.checkedRadioButtonId == -1 -> {
                showToast(stringNames.MESSAGE_NO_CARD); false
            }

            editDateText.isBlank() -> {
                showToast(stringNames.MESSAGE_NO_DATE); false
            }

            selectedType == Constance.expense && balance < money -> {
                showToast(stringNames.MESSAGE_NOT_ENOUGH_MONEY); false
            }

            else -> true
        }
    }

    private fun showToast(message: String) {
        ToastHelper.show(requireActivity(), message)
    }

    private fun showDatePicker() {
        val pickerHelper = DatePickerHelper(
            context = requireContext(),
            selectedDateMillis = selectedDateMillis,
            onDateSelected = { year, month, day, millis ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                setDate(DateUtils.parseMillisToString(cal), millis)
            }
        )

        pickerHelper.show()
    }

    private fun setDate(date: String, dateMillis: Long? = null){
        selectedDateMillis = dateMillis ?: DateUtils.parseDateToMillis(date)
        transactionDate = date

        binding.editDateText.text = DateUtils.dateSQLtoString(date)
        binding.date.text = DateUtils.dateSQLtoString(date)
    }

    private fun loadCategories(){
        selectedType = binding.operationSelector
            .findViewById<RadioButton>(binding.operationSelector.checkedRadioButtonId)
            ?.tag?.toString() ?: Constance.expense

        val query = Query.defaultForCategory(selectedType)
        categoryViewModel.loadData(query)
    }

    private fun changeOperationType(newSelectType: String? = null){

        if (newSelectType != null){
            if (newSelectType == Constance.expense)
                binding.expense.setChecked(true)
            else
                binding.admission.setChecked(true)
        }

        loadCategories()
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