package com.example.financeapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.financeapp.R
import com.example.financeapp.ui.fragments.allList.AllCardsList
import com.example.financeapp.ui.fragments.allList.AllCategoriesList
import com.example.financeapp.databinding.FragmentMoreScreenBinding
import com.example.financeapp.utils.StringNames

class MoreScreen : Fragment() {

    lateinit var binding: FragmentMoreScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMoreScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.categories.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AllCategoriesList())
                .addToBackStack(null)
                .commit()
        }

        binding.cards.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AllCardsList())
                .addToBackStack(null)
                .commit()
        }

        binding.currency.setOnClickListener {
            CurrencyDialog().show(parentFragmentManager, null)
        }

        binding.language.setOnClickListener {
            LanguageDialog().show(parentFragmentManager, null)
        }

        val packageInfo = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0)
        val versionName = packageInfo.versionName

        val stringNames = StringNames(requireContext())

        binding.appInfo.text = "${stringNames.VERSION}: $versionName"
    }
}