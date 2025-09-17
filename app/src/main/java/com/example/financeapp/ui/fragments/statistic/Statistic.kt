package com.example.financeapp.ui.fragments.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentStatisticBinding

class Statistic : Fragment() {

    private lateinit var binding: FragmentStatisticBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setActiveTab(isExpense = true)

        replaceFragment(StatisticExpense())
        binding.expenseBtn.setOnClickListener {
            setActiveTab(isExpense = true)
        }

        binding.admissionBtn.setOnClickListener {
            setActiveTab(isExpense = false)
        }
    }

    private fun setActiveTab(isExpense: Boolean){

        val colorActive = ContextCompat.getColor(requireContext(), R.color.White)
        val colorNotActive = ContextCompat.getColor(requireContext(), R.color.White60)

        binding.expenseBtn.setTextColor(if (isExpense) colorActive else colorNotActive)
        binding.admissionBtn.setTextColor(if (isExpense) colorNotActive else colorActive)

        val fragment = if (isExpense) StatisticExpense() else StatisticAdmission()
        replaceFragment(fragment)

    }

    private fun replaceFragment(fragment: Fragment){
        childFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}