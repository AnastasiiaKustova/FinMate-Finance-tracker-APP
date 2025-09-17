package com.example.financeapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.financeapp.databinding.FragmentLanguageBinding
import com.example.financeapp.ui.common.LanguageManager
import com.example.financeapp.utils.Constance.DEFAULT


class LanguageDialog: DialogFragment() {
    private lateinit var binding: FragmentLanguageBinding

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
        binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurrentLanguage()

        binding.saveButton.setOnClickListener {
            saveLanguage()
            dismiss()
        }
    }

    private fun setCurrentLanguage(){
        val language = LanguageManager.getLanguage()
        val radioButton = binding.radioGroup.findViewWithTag<RadioButton>(language)
        radioButton?.let {
            binding.radioGroup.check(it.id)
        }
    }

    private fun saveLanguage(){
        val selectedId = binding.radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val selected = view?.findViewById<RadioButton>(selectedId)?.tag?.toString() ?: DEFAULT
            LanguageManager.setLanguage(requireContext(), selected)
            activity?.recreate()
            dismiss()
        }
    }
}