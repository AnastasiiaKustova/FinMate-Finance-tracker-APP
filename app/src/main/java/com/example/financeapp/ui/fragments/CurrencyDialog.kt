package com.example.financeapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.financeapp.databinding.FragmentChangeCurrencyBinding
import com.example.financeapp.ui.common.CurrencyManager

class CurrencyDialog: DialogFragment() {
    private lateinit var binding: FragmentChangeCurrencyBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeCurrencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurrentCurrency()

        binding.saveButton.setOnClickListener {
            saveCurrency()
            dismiss()
        }
    }

    private fun setCurrentCurrency(){
        val currency = CurrencyManager.getCurrencyCode()
        val radioButton = binding.radioGroup.findViewWithTag<RadioButton>(currency)
        radioButton?.let {
            binding.radioGroup.check(it.id)
        }
    }

    private fun saveCurrency(){
        val selectedId = binding.radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val selected = view?.findViewById<RadioButton>(selectedId)?.tag?.toString() ?: CurrencyManager.getCurrency()
            CurrencyManager.changeCurrencyByCode(requireContext(), selected)
            dismiss()
        }
    }
}