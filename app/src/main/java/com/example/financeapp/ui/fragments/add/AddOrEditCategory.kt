package com.example.financeapp.ui.fragments.add

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.R
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.ui.fragments.CategoriesSelector
import com.example.financeapp.ui.adapter.ItemsCategoryAdapter
import com.example.financeapp.utils.Constance
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.databinding.FragmentAddCategoryBinding
import com.example.financeapp.ui.common.ColorHelper
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.ui.common.ToastHelper
import com.example.financeapp.utils.Constance.KEY_CATEGORY
import com.example.financeapp.utils.Constance.KEY_CATEGORY_UUID
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.utils.Constance.NEW_TAG
import com.example.financeapp.utils.Constance.REQUEST_KEY_CATEGORY
import com.example.financeapp.utils.Constance.REQUEST_KEY_NEW_CATEGORY
import com.example.financeapp.utils.StringNames
import com.example.financeapp.utils.getParcelableCompat
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.factory.ViewModelType
import kotlinx.coroutines.launch
import yuku.ambilwarna.AmbilWarnaDialog

class AddOrEditCategory : Fragment() {

    private lateinit var binding: FragmentAddCategoryBinding
    private lateinit var adapter: ItemsCategoryAdapter

    private var iconID = 0
    private var colorID = ""
    private var currentColor = 0

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var operationViewModel: OperationViewModel
    private lateinit var stringNames : StringNames
    private var selectedCategory: CategoryClass? = null
    private var selectedType: String = Constance.expense
    private var editMode: Boolean = false

    private lateinit var categories: MutableList<CategoryClass>
    private var updateCategoryId: Int = 0

    companion object {
        private const val ARG_CATEGORY = "selectedCategory"
        private const val ARG_TYPE = "selectedType"
        private const val ARG_EDIT_MODE = "editMode"

        fun newInstance(
            selectedCategory: CategoryClass?,
            selectedType: String?,
            editMode: Boolean
        ): AddOrEditCategory {
            val fragment = AddOrEditCategory()
            val args = Bundle().apply {
                putParcelable(ARG_CATEGORY, selectedCategory)
                putBoolean(ARG_EDIT_MODE, editMode)
                putString(ARG_TYPE, selectedType)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        arguments?.let {
            val args = requireArguments()
            selectedCategory = args.getParcelable(ARG_CATEGORY)
            selectedType = args.getString(ARG_TYPE) ?: Constance.expense
            editMode = args.getBoolean(ARG_EDIT_MODE)
        }

        setupViewModels()
        initAdapter()

        with(binding) {

            when (selectedType) {
                Constance.expense -> expense.setChecked(true)
                else -> admission.setChecked(true)
            }

            saveButton.setOnClickListener { saveAction() }
            backBtn.setOnClickListener { parentFragmentManager.popBackStack() }

            deleteBtn.setOnClickListener {
                selectedCategory?.let {
                    DeleteConfirmationDialog.show(requireContext()) {
                        categoryViewModel.deleteCategory(updateCategoryId)
                        parentFragmentManager.popBackStack()
                    }
                }
            }

            val randomColor: Int = ColorHelper.createRandomColor()
            setColor(randomColor)

            colorSelectBtn.setOnClickListener {
                val dialog = AmbilWarnaDialog(
                    context,
                    currentColor,
                    object : AmbilWarnaDialog.OnAmbilWarnaListener {
                        override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                            setColor(color)
                        }

                        override fun onCancel(dialog: AmbilWarnaDialog?) {}
                    })

                dialog.show()

                (dialog.dialog as? AlertDialog)?.apply {
                    getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
                    getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)

                    window?.setBackgroundDrawableResource(R.drawable.rounded_shape)
                    findViewById<View>(android.R.id.content)?.setPadding(0, 80, 0, 20)
                }

                (dialog.dialog as? AlertDialog)?.window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% ширины экрана
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
        }

        categories = CategoryIcons.getCategories(requireContext(), 9).toMutableList()
        categories.add(CategoryIcons.getMoreIcon(requireContext()))



        if (editMode && selectedCategory != null)
            loadData(selectedCategory!!)
        else
            loadCategories()
    }

    private fun setColor(color: Int) {
        currentColor = color
        colorID = ColorHelper.toHex(currentColor)
        binding.colorSelectBtn.background.setTint(currentColor)
        adapter.updateColor(colorID)
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

        categoryViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        categoryViewModel.categoryCreatedUUID.observe(viewLifecycleOwner) { categoryUUID ->

            if (categoryUUID != "" && categoryUUID != null) {
                val result = Bundle().apply {
                    putString(KEY_CATEGORY_UUID, categoryUUID)
                }
                parentFragmentManager.setFragmentResult(REQUEST_KEY_NEW_CATEGORY, result)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun initAdapter() {

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_CATEGORY,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedCategory = bundle.getParcelableCompat<CategoryClass>(KEY_CATEGORY)
            selectedCategory?.let {
                loadCategories(selectedCategory)
            }
        }

        adapter = ItemsCategoryAdapter(emptyList())

        binding.itemCategory.layoutManager = GridLayoutManager(binding.root.context, 5)
        binding.itemCategory.adapter = adapter

        adapter.onItemClick = { it, position ->

            if (it.title == stringNames.MORE_TITLE) {
                CategoriesSelector.newInstance(it, selectedType, NEW_TAG)
                    .show((activity as FragmentActivity).supportFragmentManager, null)
            } else {
                selectedCategory = it
                adapter.setSelectedPosition(position)
                iconID = it.iconId
            }
        }
    }

    private fun loadData(selectedCategory: CategoryClass) {
        with(binding) {
            val operations = operationViewModel.findOperationByCategory(selectedCategory.id.toString())
            if (operations.isNotEmpty())
                for (i in 0 until binding.operationSelector.childCount) {
                    binding.operationSelector.getChildAt(i).isEnabled = false
                }
            else
                for (i in 0 until binding.operationSelector.childCount) {
                    binding.operationSelector.getChildAt(i).isEnabled = true
                }

            title.visibility = View.GONE
            titleUpdate.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
            saveButton.text = stringNames.SAVE_TITLE

            binding.titleText.setText(selectedCategory.title)

            setColor(ColorHelper.fromHex(selectedCategory.colorId))

            updateCategoryId = selectedCategory.id
        }

        loadCategories(selectedCategory)
    }

    private fun saveAction() {

        if (!canSave()) return

        val title = binding.titleText.text.toString().trim()
        val type = binding.operationSelector
            .findViewById<RadioButton>(binding.operationSelector.checkedRadioButtonId)
            ?.tag?.toString() ?: Constance.expense

        val newCategory = CategoryClass(
            id = updateCategoryId,
            title = title,
            typeOperation = type,
            iconId = iconID,
            colorId = colorID
        )

        // Проверка на дубликат по title + typeOperation
        viewLifecycleOwner.lifecycleScope.launch {
            val existing = categoryViewModel.findCategoryByTitleAndType(title, selectedType)

            val isDuplicate = existing != null && (!editMode && existing.id != newCategory.id)

            if (isDuplicate) {
                Toast.makeText(
                    requireContext(),
                    stringNames.MESSAGE_CATEGORY_EXISTS,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            if (editMode) {
                categoryViewModel.updateCategory(newCategory)
                parentFragmentManager.popBackStack()
            } else {
                categoryViewModel.addCategory(newCategory)
            }


        }
    }

    private fun canSave(): Boolean {
        val title = binding.titleText.text.toString().trim()

        return when {
            title.isBlank() -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_TITLE); false
            }

            iconID == 0 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_ICON); false
            }

            binding.operationSelector.checkedRadioButtonId == -1 -> {
                ToastHelper.show(requireContext(), stringNames.MESSAGE_NO_TYPE); false
            }

            else -> true
        }
    }

    private fun loadCategories(category: CategoryClass? = null) {

        category?.let {
            var findedIndex = -1
            for ((i, c) in categories.withIndex()) {
                if (c.iconId == category.iconId)
                    findedIndex = i
            }

            if (findedIndex != -1)
                categories.removeAt(findedIndex)
            else
                categories.removeAt(8)

            categories.add(0, it.copy(title = ""))
        }

        adapter.updateData(categories)

        selectedCategory = null

        if (categories.isNotEmpty()) {
            //if (categories[0].id != 0){
                selectedCategory = categories[0]
            //}
            adapter.setSelectedPosition(0)
        }
        else
            adapter.unselectPosition()

        iconID = selectedCategory?.iconId ?: 0
    }
}