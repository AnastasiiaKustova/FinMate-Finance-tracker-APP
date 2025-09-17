package com.example.financeapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.ui.fragments.add.AddOrEditCategory
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.databinding.FragmentAllCategoriesBinding
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.utils.Constance.KEY_CATEGORY
import com.example.financeapp.utils.Constance.NEW_TAG
import com.example.financeapp.utils.Constance.REQUEST_KEY_CATEGORY
import com.example.financeapp.utils.StringNames
import com.example.financeapp.viewModel.factory.ViewModelType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategoriesSelector : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentAllCategoriesBinding
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var selectedCategory : CategoryClass
    private lateinit var selectedType : String
    private lateinit var adapter: ItemsCategoryAdapter
    private lateinit var stringNames : StringNames
    private var sourceTag: String? = null

    companion object {
        private const val ARG_CATEGORY = "selectedCategory"
        private const val ARG_TYPE = "selectedType"
        private const val ARG_TAG = "tag"

        fun newInstance(selectedCategory: CategoryClass, selectedType: String, tag: String): CategoriesSelector {
            val fragment = CategoriesSelector()
            val args = Bundle().apply {
                putParcelable(ARG_CATEGORY, selectedCategory)
                putString(ARG_TYPE, selectedType)
                putString(ARG_TAG, tag)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            ) ?: return

            val behavior = BottomSheetBehavior.from(bottomSheet)

            // делаем высоту "match_parent", если хотим fullscreen
            if (sourceTag == NEW_TAG) {
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true // чтобы сразу развернулся
            } else {
                // обычное поведение
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.skipCollapsed = false
            }
        }
    }

    private fun showErrorAndExit() {
        ToastHelper.show(requireContext(), stringNames.CATEGORY_ERROR)
        parentFragmentManager.popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()

        arguments?.let {
            val args = requireArguments()
            selectedCategory = args.getParcelable(ARG_CATEGORY)
                ?: return showErrorAndExit()

            selectedType = args.getString(ARG_TYPE).orEmpty()
            if (selectedType.isBlank()) return showErrorAndExit()
            sourceTag = it.getString(ARG_TAG)
        }

        initCategoryAdapter()

       if (sourceTag == NEW_TAG)
       {
           loadIcons()
           binding.title.text = stringNames.ALL_ICONS_TITLE
       }
        else
            loadCategories()

        binding.saveButton.setOnClickListener {
            saveButton()
        }
    }

    private fun loadCategories(){

        categoryViewModel.categories.observe(viewLifecycleOwner){categories ->
            if (!::selectedCategory.isInitialized && categories.isNotEmpty()) {
                selectedCategory = categories[0]
            }
            adapter.updateData(categories.sortedBy { it.title })
        }
    }

    private fun setupViewModels(){
        categoryViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CATEGORY
        )
    }

    private fun loadIcons(){
        val categories = CategoryIcons.getCategories(requireContext())
        adapter.updateData(categories)

        if (categories.size > 0)
            selectedCategory = categories[0]
    }

    private fun initCategoryAdapter(){
        adapter = ItemsCategoryAdapter(emptyList())

        binding.itemCategory.layoutManager = GridLayoutManager(binding.root.context, 5)
        binding.itemCategory.adapter = adapter

        adapter.onItemClick = { it, position ->
            if (it.title == stringNames.ADD_TITLE)
            {
                parentFragmentManager.beginTransaction()
                    .replace(com.example.financeapp.R.id.frame_layout, AddOrEditCategory())
                    .addToBackStack(null)
                    .commit()
            }
            else
            {
                selectedCategory = it
                adapter.setSelectedPosition(position)
            }
        }
    }

    private fun saveButton(){
        if (!::selectedCategory.isInitialized) {
            Toast.makeText(requireContext(), stringNames.MESSAGE_NO_CATEGORY, Toast.LENGTH_SHORT).show()
            return
        }

        val result = Bundle().apply {
            putParcelable(KEY_CATEGORY, selectedCategory)
        }
        parentFragmentManager.setFragmentResult(REQUEST_KEY_CATEGORY, result)
        dismiss()
    }
}