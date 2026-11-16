package com.example.tcg_manager.ui.card

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tcg_manager.R
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.databinding.FragmentCardDetailBinding
import com.example.tcg_manager.ui.collection.CollectionViewModel
import com.example.tcg_manager.ui.scan.ScanViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Shows every field of the selected card and offers to add it to the collection. */
class CardDetailFragment : Fragment() {

    private var _binding: FragmentCardDetailBinding? = null
    private val binding get() = _binding!!

    private val scanViewModel: ScanViewModel by activityViewModels { ScanViewModel.Factory }
    private val collectionViewModel: CollectionViewModel by viewModels { CollectionViewModel.Factory }

    private var ownedQuantity = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val card = scanViewModel.selectedCard.value
        if (card == null) {
            findNavController().navigateUp()
            return
        }
        bind(card)
        binding.addButton.setOnClickListener { askToAdd(card) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectionViewModel.quantity(card.key).collectLatest { quantity ->
                    ownedQuantity = quantity
                    binding.ownedText.text = getString(R.string.detail_owned, quantity)
                }
            }
        }

        // Ask once, right after a scan/search. Not again on rotation or when opened from the collection.
        if (arguments?.getBoolean(ARG_ASK_TO_ADD) == true) {
            arguments?.putBoolean(ARG_ASK_TO_ADD, false)
            binding.root.post { if (_binding != null) askToAdd(card) }
        }
    }

    private fun bind(card: Card) = with(binding) {
        nameText.text = card.name
        subtitleText.setOrHide(
            listOfNotNull(
                card.game.displayName,
                card.setName?.let { name -> card.setCode?.let { "$name ($it)" } ?: name } ?: card.setCode,
                card.collectorNumber?.let { "#$it" },
                card.rarity,
            ).joinToString(" · ")
        )
        typeText.setOrHide(card.typeLine)
        costText.setOrHide(card.cost?.let { getString(R.string.detail_cost, it) })
        statsText.setOrHide(card.stats.entries.joinToString("\n") { (k, v) -> "$k: $v" }, statsLabel)
        descriptionText.setOrHide(card.description, descriptionLabel)
        flavorText.setOrHide(card.flavorText, flavorLabel)
        pricesText.setOrHide(card.prices.entries.joinToString("\n") { (k, v) -> "$k: $v" }, pricesLabel)
        sourceText.text = listOfNotNull(
            card.artist?.let { getString(R.string.detail_artist, it) },
            getString(R.string.detail_source, card.source, card.externalId),
            card.imageUrl,
        ).joinToString("\n")
    }

    private fun askToAdd(card: Card) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("1")
            setSelectAllOnFocus(true)
        }
        val container = FrameLayout(requireContext()).apply {
            val pad = resources.getDimensionPixelSize(R.dimen.dialog_padding)
            setPadding(pad, 0, pad, 0)
            addView(input)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.detail_add_title)
            .setMessage(getString(R.string.detail_add_message, card.name, ownedQuantity))
            .setView(container)
            .setPositiveButton(R.string.detail_add_confirm) { _, _ ->
                val amount = input.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: 1
                collectionViewModel.add(card, amount)
                _binding?.let {
                    Snackbar.make(it.root, getString(R.string.detail_added, amount, card.name), Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.detail_add_cancel, null)
            .show()
    }

    private fun TextView.setOrHide(value: String?, label: View? = null) {
        val visible = !value.isNullOrBlank()
        text = value
        isVisible = visible
        label?.isVisible = visible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_ASK_TO_ADD = "ask_to_add"
    }
}
