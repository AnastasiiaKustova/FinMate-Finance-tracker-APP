package com.example.financeapp.ui.fragments.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.databinding.FragmentAddMoneyTransactionBinding
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.ui.common.DatePickerHelper
import com.example.financeapp.ui.common.SpinnerUtils
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.utils.CommentUtils
import com.example.financeapp.ui.common.CurrencyManager
import com.example.financeapp.utils.MoneyTextWatcher
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.factory.ViewModelType
import java.util.Calendar

class AddMoneyTransaction : Fragment() {

    private lateinit var binding : FragmentAddMoneyTransactionBinding
    private var selectedDateMillis: Long? = null

    private lateinit var operationViewModel: OperationViewModel
    private lateinit var cardsViewModel: CardsViewModel
    private lateinit var watcher: MoneyTextWatcher

    private var cards: List<CardsClass> = emptyList()
    private lateinit var transactionDate: String
    private lateinit var stringNames : StringNames

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddMoneyTransactionBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        stringNames = StringNames(requireContext())

        setupViewModels()
        setCards()

        with(binding){
            textCurrency.text = CurrencyManager.getCurrency()

            transactionDate = DateUtils.getCurrentDate()
            editDateText.text = DateUtils.dateSQLtoString(transactionDate)

            watcher = MoneyTextWatcher(moneyText)
            moneyText.addTextChangedListener(watcher)

            addButton.setOnClickListener { saveAction() }
            backButton.setOnClickListener{ parentFragmentManager.popBackStack() }
            editDateText.setOnClickListener { showDatePicker() }
            icCalendar.setOnClickListener { showDatePicker() }
        }
    }

    private fun setupViewModels(){
        cardsViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CARD
        )

        operationViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.TRANSACTION
        )
    }

    private fun setCards(){
        cardsViewModel.cards.observe(viewLifecycleOwner){ cards ->
            this.cards = cards

            if (cards.isEmpty()) {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_CARD_NOT_EXIST)
                parentFragmentManager.popBackStack()
            }

            SpinnerUtils.setupSpinnerCards(requireContext(), binding.cardFrom, cards)
            SpinnerUtils.setupSpinnerCards(requireContext(), binding.cardTo, cards)
        }

        cardsViewModel.loadData()
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

    private fun saveAction() {

        if (!canSave()) return

        val comment = binding.comment.text.toString()
        val moneyInt = watcher.getValue()

        val cardFrom = binding.cardFrom.selectedItem as CardsClass
        val cardTo = binding.cardTo.selectedItem as CardsClass

        operationViewModel.addTransaction(
            OperationClass(
                type = Constance.minus,
                date = transactionDate,
                comment = CommentUtils.encodeComment(comment,cardTo.UUID),
                cardUUID = cardFrom.UUID,
                money = moneyInt
            )
        )

        operationViewModel.addTransaction(
            OperationClass(
                type = Constance.plus,
                date = transactionDate,
                comment = comment,
                cardUUID = cardTo.UUID,
                money = moneyInt
            )
        )

        parentFragmentManager.popBackStack()
    }

    private fun canSave() : Boolean{

        val cardFrom = binding.cardFrom.selectedItem as CardsClass
        val cardTo = binding.cardTo.selectedItem as CardsClass

        val cardFromClass = cards.find { it.id ==  cardFrom.id} ?: return false
        val cardBalance = cardsViewModel.getBalance(cardFromClass)

        val moneyInt = watcher.getValue()

        return when {
            moneyInt <= 0 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_MONEY); false
            }
            cardFrom == cardTo ->{
                ToastHelper.show(requireContext(), stringNames.MESSAGE_CARD_EQUIV); false
            }
            cardBalance < moneyInt ->{
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NOT_ENOUGH_MONEY); false
            }

            else -> true
        }
    }
}