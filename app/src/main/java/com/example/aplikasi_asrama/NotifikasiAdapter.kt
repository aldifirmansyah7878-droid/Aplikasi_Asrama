package com.example.aplikasi_asrama.notifikasi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.api.model.NotifikasiData
import com.example.aplikasi_asrama.databinding.ItemNotifikasiBinding

class NotifikasiAdapter(
    private val onItemClick: (NotifikasiData) -> Unit
) : RecyclerView.Adapter<NotifikasiAdapter.ViewHolder>() {

    private var items: List<NotifikasiData> = emptyList()

    fun submitList(list: List<NotifikasiData>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifikasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        // ===== KLIK ITEM =====
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemNotifikasiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotifikasiData) {
            binding.tvJudul.text = item.judul
            binding.tvPesan.text = item.pesan
            binding.tvWaktu.text = item.createdAt

            if (item.isRead == 1) {
                binding.tvStatus.text = "Dibaca"
                binding.tvStatus.setBackgroundColor(
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                )
            } else {
                binding.tvStatus.text = "Baru"
                binding.tvStatus.setBackgroundColor(
                    binding.root.context.getColor(android.R.color.holo_red_dark)
                )
            }
        }
    }
}