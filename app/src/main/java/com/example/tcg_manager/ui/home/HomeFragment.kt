package com.example.tcg_manager.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tcg_manager.R
import com.example.tcg_manager.databinding.FragmentHomeBinding
import com.example.tcg_manager.databinding.ItemGameWidgetBinding

/** Main screen: one widget per supported trading card game. */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidget(binding.widgetMagic, HomeGame.MAGIC)
        bindWidget(binding.widgetPokemon, HomeGame.POKEMON)
        bindWidget(binding.widgetYugioh, HomeGame.YUGIOH)
    }

    private fun bindWidget(widget: ItemGameWidgetBinding, game: HomeGame) {
        widget.gameName.setText(game.title)
        widget.gameBadge.text = game.initials
        widget.gameBadge.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), game.color))
        // Placeholder until the collection counts per game are wired in.
        widget.cardCount.text = getString(R.string.home_count_placeholder)
        widget.cardCountCaption.setText(R.string.home_count_caption)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
