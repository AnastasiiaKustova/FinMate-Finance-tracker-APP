package com.example.financeapp.ui.fragments.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.utils.Constance
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.databinding.FragmentAddNewCardBinding
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.utils.MoneyTextWatcher
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.factory.ViewModelType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddNewCard : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddNewCardBinding
    private lateinit var cardsViewModel: CardsViewModel
    private lateinit var operationViewModel: OperationViewModel
    private lateinit var watcher: MoneyTextWatcher
    private lateinit var stringNames : StringNames

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        stringNames = StringNames(requireContext())

        setupViewModels()

        watcher = MoneyTextWatcher(binding.balance)
        binding.balance.addTextChangedListener(watcher)

        binding.addButton.setOnClickListener {
            saveAction()
        }
    }

    private fun setupViewModels() {
        operationViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.TRANSACTION
        )

        cardsViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CARD
        )

        cardsViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        cardsViewModel.cardCreatedUUID.observe(viewLifecycleOwner) { newUUID ->
            if (newUUID != "" && newUUID != null) {
                val today = DateUtils.getCurrentDate()
                val moneyInt = watcher.getValue()

                if (moneyInt != 0) {
                    val newTransaction = OperationClass(
                        type = Constance.create,
                        date = today,
                        cardUUID = newUUID,
                        money = moneyInt,
                        comment = ""
                    )
                    operationViewModel.addTransaction(newTransaction)
                }
                dismiss()
            }
        }
    }

    private fun canSave(): Boolean {

        val title = binding.title.text.toString().trim()
        return when {

            title.isBlank() -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_TITLE); false
            }

            else -> true
        }
    }

    private fun saveAction() {

        if (!canSave()) return

        val title = binding.title.text.toString()
        val comment = binding.comment.text.toString()
        val today = DateUtils.getCurrentDate()

        val newCard = CardsClass(
            title = title,
            date = today,
            visible = 1,
            comment = comment
        )

        cardsViewModel.addCard(newCard)
    }
}