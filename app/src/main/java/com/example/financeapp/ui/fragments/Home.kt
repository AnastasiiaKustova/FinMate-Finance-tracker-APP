package com.example.financeapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.R
import com.example.financeapp.data.dbManager.OperationTypes
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.ui.adapter.CardsAdapter
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.ui.adapter.ItemsWithDateAdapter
import com.example.financeapp.ui.fragments.add.AddMoneyTransaction
import com.example.financeapp.ui.fragments.add.AddNewCard
import com.example.financeapp.ui.fragments.allList.AllTransaction
import com.example.financeapp.ui.fragments.info.CardInfo
import com.example.financeapp.ui.fragments.info.TransactionInfo
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.CategoryViewModel
import com.example.financeapp.viewModel.OperationViewModel
import com.example.financeapp.viewModel.SettingsViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.useCase.settings.Settings
import com.example.financeapp.databinding.FragmentHomeBinding
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.viewModel.factory.ViewModelType
import com.example.financeapp.useCase.settings.SettingsQueryBuilder
import com.example.financeapp.utils.Constance.EDIT_TAG
import com.example.financeapp.utils.Constance.INFO_TAG
import com.example.financeapp.utils.MoneyFormatter
import com.example.financeapp.utils.StringNames

class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var detailsVisibility = View.GONE

    private lateinit var operationViewModel: OperationViewModel
    private lateinit var cardsViewModel: CardsViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    private lateinit var adapter: ItemsWithDateAdapter
    private lateinit var cardsAdapter: CardsAdapter
    private lateinit var stringNames : StringNames

    private var cardUUIDs: List<CardsClass> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringNames = StringNames(requireContext())

        setupViewModels()
        initListeners()
        initCardsAdapter()
        initOperationAdapter()
        setCards()
        setCategories()
        setOperations()

    }

    fun setupViewModels() {
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

        categoryViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CATEGORY
        )

        settingsViewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]
    }

    private fun initListeners() {
        with(binding) {
            more.setOnClickListener {
                settingsViewModel.updateSettings(Settings.default())

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, AllTransaction())
                    .addToBackStack(null)
                    .commit()
            }

            showDetailsBtn.setOnClickListener {
                if (detailsVisibility == View.GONE) {
                    balanceDetails.visibility = View.VISIBLE
                    detailsVisibility = View.VISIBLE
                    showDetailsBtn.setImageResource(R.drawable.ic_buttondown)
                } else {
                    balanceDetails.visibility = View.GONE
                    detailsVisibility = View.GONE
                    showDetailsBtn.setImageResource(R.drawable.ic_buttonup)
                }
            }

            addCardBtn.setOnClickListener {
                AddNewCard().show((activity as FragmentActivity).supportFragmentManager, null)
            }

            switchCardBtn.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, AddMoneyTransaction())
                    .addToBackStack(null)
                    .commit()
            }

            historyBtn.setOnClickListener {
                settingsViewModel.updateSettings(
                    Settings.default().copy(
                        operationTypes = arrayListOf(stringNames.TRANSFERS)
                    )
                )

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, AllTransaction())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun initCardsAdapter() {
        cardsAdapter = CardsAdapter(emptyList())
        binding.itemsBalance.layoutManager = LinearLayoutManager(requireContext())
        binding.itemsBalance.adapter = cardsAdapter

        cardsAdapter.onItemClick = { card ->
            val fragment = CardInfo.newInstance(card, INFO_TAG)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
        cardsAdapter.onVisibilityToggle = { card ->
            cardsViewModel.toggleCardVisibility(card)
        }

        cardsAdapter.onDeleteClick = { card ->
            DeleteConfirmationDialog.show(requireContext()) {
                cardsViewModel.deleteFromDb(card)
            }
        }

        cardsAdapter.onEditClick = { card ->
            val fragment = CardInfo.newInstance(card, EDIT_TAG)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initOperationAdapter() {
        with(binding) {
            adapter = ItemsWithDateAdapter(emptyList(), emptyList(), emptyList())
            operationList.layoutManager = LinearLayoutManager(requireContext())
            operationList.adapter = adapter

            adapter.onItemClick = { item ->
                val fragment = TransactionInfo.newInstance(item, INFO_TAG)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            adapter.onDeleteClick = { item ->
                DeleteConfirmationDialog.show(requireContext()) {
                    operationViewModel.deleteFromDb(item)
                }
            }

            adapter.onEditClick = { transaction ->
                when (transaction.type) {
                    Constance.expense, Constance.admission -> {
                        val fragment = TransactionInfo.newInstance(transaction, EDIT_TAG)
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.frame_layout,
                                fragment
                            )
                            .addToBackStack(null)
                            .commit()
                    }

                    else -> Toast.makeText(
                        requireActivity(),
                        stringNames.ERROR_EDIT_MESSAGE,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun setCards() {
        cardsViewModel.cards.observe(viewLifecycleOwner) { dataListCards ->

            adapter.updateCards(dataListCards)

            cardUUIDs = dataListCards
                .filter { it.visible == 1 }

            if (cardUUIDs.isNotEmpty())
                binding.noCardInfo.visibility = View.GONE
            else
                binding.noCardInfo.visibility = View.VISIBLE

            cardsAdapter.updateData(cardUUIDs)

            if (cardUUIDs.isEmpty())
                binding.balance.text = MoneyFormatter.format(0)
            else {
                val query =
                    SettingsQueryBuilder(Settings.default(), requireContext()).buildForBalance(cardUUIDs.map { it.UUID })
                binding.balance.text = MoneyFormatter.format(operationViewModel.getBalance(query))
            }
        }

        cardsViewModel.loadData()
    }

    private fun setCategories() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateCategories(categories)
        }

        categoryViewModel.loadData(Query.empty())
    }

    private fun setOperations() {
        with(binding) {

            operationViewModel.operations.observe(viewLifecycleOwner) { operations ->
                if (operations.isNotEmpty())
                    noTransactionInfo.visibility = View.GONE
                else
                    noTransactionInfo.visibility = View.VISIBLE
                adapter.updateItems(operations)

                cardsViewModel.loadData()
            }

            operationViewModel.loadDataWithQuery(
                SettingsQueryBuilder(
                    Settings.default().copy(
                        dateStart = DateUtils.getYesterday(),
                        dateEnd = DateUtils.getCurrentDate(),
                        operationTypesExclude = arrayListOf(Constance.plus)
                    ), requireContext()
                ).build(),
                OperationTypes.DATE
            )
        }
    }
}