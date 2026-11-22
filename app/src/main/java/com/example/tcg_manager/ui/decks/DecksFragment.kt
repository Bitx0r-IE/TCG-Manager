package com.example.tcg_manager.ui.decks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tcg_manager.databinding.FragmentDecksBinding

/** Decks tab. Placeholder until deck building is implemented. */
class DecksFragment : Fragment() {

    private var _binding: FragmentDecksBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDecksBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
