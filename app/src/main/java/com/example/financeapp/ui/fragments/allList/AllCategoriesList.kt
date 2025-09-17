package com.example.financeapp.ui.fragments.allList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.ui.fragments.add.AddOrEditCategory
import com.example.financeapp.ui.adapter.ItemsCategoryListAdapter
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.databinding.FragmentAllCategoriesListBinding
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.useCase.settings.SettingsQueryBuilder
import com.example.financeapp.viewModel.factory.ViewModelType
import com.google.android.material.chip.Chip

class AllCategoriesList : Fragment() {

    private lateinit var binding: FragmentAllCategoriesListBinding
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var adapter: ItemsCategoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllCategoriesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModels()
        initAdapter()
        setCategory()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.addCategoryBtn.setOnClickListener {
            val fragment = AddOrEditCategory.newInstance(null, null, false)
            parentFragmentManager.beginTransaction()
                .replace(com.example.financeapp.R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }

        val checkedList = arrayListOf<String>()

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                checkedList.clear()
                checkedIds.forEach { idx ->
                    val chip = view.findViewById<Chip>(idx)
                    checkedList.add(chip.text.toString())
                }

                val query = SettingsQueryBuilder(Settings.default(), requireContext()).buildForCategoryByList(checkedList)
                categoryViewModel.loadData(query)
            }
        }
    }

    private fun setupViewModels(){
        categoryViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CATEGORY
        )
    }

    private fun initAdapter(){
        adapter = ItemsCategoryListAdapter(emptyList())
        binding.dataList.layoutManager = LinearLayoutManager(requireContext())
        binding.dataList.adapter = adapter

        adapter.onItemClick = { it, position ->
            val fragment =
                AddOrEditCategory.newInstance(it, it.typeOperation, true)
            parentFragmentManager.beginTransaction()
                .replace(com.example.financeapp.R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setCategory(){
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.emptyText.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
            adapter.updateData(categories)
        }

        categoryViewModel.loadData(Query.defaultForAllCategory())
    }
}