package com.example.financeapp.ui.fragments.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.databinding.FragmentCardInfoBinding
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.utils.Constance.EDIT_TAG
import com.example.financeapp.utils.MoneyFormatter
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.viewModel.factory.ViewModelType

class CardInfo : Fragment() {

    private lateinit var binding: FragmentCardInfoBinding
    private lateinit var cardsViewModel: CardsViewModel
    private lateinit var card : CardsClass
    private lateinit var stringNames : StringNames
    private var visibility: Int = 1
    private var sourceTag: String? = null

    companion object {
        private const val ARG_CARD = "card"
        private const val ARG_TAG = "tag"

        fun newInstance(card: CardsClass, tag: String): CardInfo {
            val fragment = CardInfo()
            val args = Bundle().apply {
                putParcelable(ARG_CARD, card)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stringNames = StringNames(requireContext())

        arguments?.let {
            val cardArg = it.getParcelable<CardsClass>(ARG_CARD)
            if (cardArg == null) {
                showErrorAndExit()
            } else {
                card = cardArg
                visibility = card.visible
                if (visibility == 0)
                    binding.visibleSelector.check(binding.unvisibleEdit.id)
                else
                    binding.visibleSelector.check(binding.visibleEdit.id)
            }
            sourceTag = it.getString(ARG_TAG)
        }

        setupViewModels()

        if (sourceTag == EDIT_TAG)
            initEdit()
        else
            initInfo()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupViewModels(){
        cardsViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CARD
        )
    }

    private fun initInfo(){
        with(binding) {
            infoLayout.visibility = View.VISIBLE
            editLayout.visibility = View.GONE

            title.text = card.title
            date.text = DateUtils.dateSQLtoString(card.date)
            balance.text = MoneyFormatter.format(card.balance)

            visible.text = if (card.visible == 0)
                stringNames.CARD_UNVISIBLE
            else
                stringNames.CARD_VISIBLE

            if (card.comment.isNotEmpty())
                comment.text = card.comment
            else
                comment.text = stringNames.NO_COMMENT_TEXT

            editOrDeleteBtn.setOnClickListener {
                initEdit()
            }

            deleteBtn.setOnClickListener{
                DeleteConfirmationDialog.show(requireContext()){
                    cardsViewModel.deleteFromDb(card)
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun initEdit(){
        with(binding) {
            infoLayout.visibility = View.GONE
            editLayout.visibility = View.VISIBLE

            title.text = stringNames.CARD_EDIT
            editOrDeleteBtn.setImageResource(CategoryIcons.deleteIcon)

            titleEditText.setText(card.title)
            balanceEdit.text = MoneyFormatter.format(card.balance)

            if (card.comment.isNotEmpty())
                commentEditText.setText(card.comment)

            editOrDeleteBtn.setOnClickListener {
                DeleteConfirmationDialog.show(requireContext()){
                    cardsViewModel.deleteFromDb(card)
                    parentFragmentManager.popBackStack()
                }
            }

            saveBtn.setOnClickListener{
                save()
            }
        }
    }

    private fun save(){
        if (!canSave()) return

        val visibilityText = binding.visibleSelector.findViewById<RadioButton>(binding.visibleSelector.checkedRadioButtonId)?.text?.toString()?.trim()

        visibility = if (visibilityText == stringNames.UNVISIBLE_TITLE)
            0
        else
            1

        val newCard = card.copy(
            title = binding.titleEditText.text.toString(),
            comment = binding.commentEditText.text.toString(),
            visible = visibility
        )

        cardsViewModel.updateCard(newCard)
        parentFragmentManager.popBackStack()
    }

    private fun canSave():Boolean{
        val title = binding.title.text.toString().trim()

        return when {

            title.isBlank() -> {
                ToastHelper.show(requireContext(),stringNames.MESSAGE_NO_TITLE); false
            }

            else -> true
        }
    }
}