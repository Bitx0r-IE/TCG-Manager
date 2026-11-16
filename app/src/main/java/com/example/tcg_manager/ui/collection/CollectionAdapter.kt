package com.example.tcg_manager.ui.collection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tcg_manager.data.model.CollectionCard
import com.example.tcg_manager.databinding.ItemCollectionCardBinding

class CollectionAdapter(
    private val onClick: (CollectionCard) -> Unit,
    private val onIncrement: (CollectionCard) -> Unit,
    private val onDecrement: (CollectionCard) -> Unit,
) : ListAdapter<CollectionCard, CollectionAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCollectionCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCollectionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CollectionCard) = with(binding) {
            val card = item.card
            nameText.text = card.name
            subtitleText.text = listOfNotNull(
                card.game.displayName,
                card.setName ?: card.setCode,
                card.collectorNumber?.let { "#$it" },
            ).joinToString(" · ")
            quantityText.text = item.quantity.toString()
            root.setOnClickListener { onClick(item) }
            plusButton.setOnClickListener { onIncrement(item) }
            minusButton.setOnClickListener { onDecrement(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<CollectionCard>() {
        override fun areItemsTheSame(old: CollectionCard, new: CollectionCard) = old.card.key == new.card.key
        override fun areContentsTheSame(old: CollectionCard, new: CollectionCard) = old == new
    }
}
