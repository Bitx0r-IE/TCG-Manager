package com.example.tcg_manager.ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tcg_manager.R
import com.example.tcg_manager.data.model.CollectionCard
import com.example.tcg_manager.databinding.FragmentCollectionBinding
import com.example.tcg_manager.ui.card.CardDetailFragment
import com.example.tcg_manager.ui.scan.ScanViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Lists saved cards with their quantity; tap to see details, +/- to change the amount. */
class CollectionFragment : Fragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectionViewModel by viewModels { CollectionViewModel.Factory }
    private val scanViewModel: ScanViewModel by activityViewModels { ScanViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CollectionAdapter(
            onClick = ::openCard,
            onIncrement = { viewModel.setQuantity(it.card.key, it.quantity + 1) },
            onDecrement = ::decrement,
        )
        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cards.collect { cards ->
                    if (cards == null) return@collect
                    adapter.submitList(cards)
                    binding.emptyText.isVisible = cards.isEmpty()
                    binding.summaryText.text = getString(
                        R.string.collection_summary, cards.size, cards.sumOf { it.quantity }
                    )
                }
            }
        }
    }

    private fun openCard(item: CollectionCard) {
        scanViewModel.select(item.card)
        findNavController().navigate(
            R.id.action_collection_to_cardDetail,
            bundleOf(CardDetailFragment.ARG_ASK_TO_ADD to false),
        )
    }

    private fun decrement(item: CollectionCard) {
        if (item.quantity > 1) {
            viewModel.setQuantity(item.card.key, item.quantity - 1)
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.collection_remove_title)
            .setMessage(getString(R.string.collection_remove_message, item.card.name))
            .setPositiveButton(R.string.collection_remove_confirm) { _, _ ->
                viewModel.setQuantity(item.card.key, 0)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
