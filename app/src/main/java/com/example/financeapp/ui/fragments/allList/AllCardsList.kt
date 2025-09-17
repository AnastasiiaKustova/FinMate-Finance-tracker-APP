package com.example.financeapp.ui.fragments.allList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.R
import com.example.financeapp.ui.fragments.add.AddNewCard
import com.example.financeapp.ui.fragments.info.CardInfo
import com.example.financeapp.ui.adapter.CardsAdapter
import com.example.financeapp.viewModel.CardsViewModel
import com.example.financeapp.viewModel.factory.ViewModelProviderHelper
import com.example.financeapp.databinding.FragmentAllCardsListBinding
import com.example.financeapp.ui.common.DeleteConfirmationDialog
import com.example.financeapp.useCase.settings.Query
import com.example.financeapp.utils.Constance.ALL_TAG
import com.example.financeapp.utils.Constance.EDIT_TAG
import com.example.financeapp.utils.Constance.INFO_TAG
import com.example.financeapp.viewModel.factory.ViewModelType

class AllCardsList : Fragment() {

    private lateinit var binding: FragmentAllCardsListBinding
    private lateinit var cardsViewModel: CardsViewModel
    private lateinit var adapter: CardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllCardsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModels()
        initAdapter()
        setCards()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.addCardBtn.setOnClickListener {
            AddNewCard().show(
                (activity as FragmentActivity).supportFragmentManager, null
            )
            cardsViewModel.loadData(Query.defaultForCards().copy(selection = null, selectionArgs = null))
        }
    }

    private fun setupViewModels(){
        cardsViewModel = ViewModelProviderHelper.getViewModel(
            requireActivity(),
            requireContext(),
            ViewModelType.CARD
        )
    }

    private fun initAdapter(){
        adapter = CardsAdapter(emptyList(), ALL_TAG)
        binding.dataList.layoutManager = LinearLayoutManager(requireContext())
        binding.dataList.adapter = adapter

        adapter.onItemClick = { card ->
            val fragment = CardInfo.newInstance(card, INFO_TAG)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }

        adapter.onVisibilityToggle = { card ->
            cardsViewModel.toggleCardVisibility(card)
        }

        adapter.onDeleteClick ={ card ->
            DeleteConfirmationDialog.show(requireContext()){
                cardsViewModel.deleteFromDb(card)
                parentFragmentManager.popBackStack()
            }
        }
        adapter.onEditClick ={ card ->
            val fragment = CardInfo.newInstance(card, EDIT_TAG)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setCards(){
        cardsViewModel.cards.observe(viewLifecycleOwner) { dataList ->
            binding.emptyText.visibility = if (dataList.isEmpty()) View.VISIBLE else View.GONE
            adapter.updateData(dataList)
        }

        cardsViewModel.loadData(Query.defaultForCards().copy(selection = null, selectionArgs = null))
    }
}