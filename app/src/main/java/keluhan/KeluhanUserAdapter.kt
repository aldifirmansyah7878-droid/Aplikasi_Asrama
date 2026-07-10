package com.example.aplikasi_asrama.keluhan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.KeluhanData
import com.example.aplikasi_asrama.databinding.ItemKeluhanUserBinding

class KeluhanUserAdapter(
    private val onItemClick: (KeluhanData) -> Unit
) : RecyclerView.Adapter<KeluhanUserAdapter.ViewHolder>() {

    private var items: List<KeluhanData> = emptyList()

    fun submitList(newList: List<KeluhanData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKeluhanUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener { onItemClick(items[position]) }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemKeluhanUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KeluhanData) {
            binding.tvJudul.text = item.judulKeluhan
            binding.tvDeskripsi.text = item.deskripsiKeluhan
            binding.tvTanggal.text = item.tanggalKeluhan

            val statusText = when (item.status) {
                "menunggu" -> "Menunggu"
                "proses" -> "Diproses"
                "selesai" -> "Selesai"
                else -> "Menunggu"
            }
            binding.tvStatus.text = statusText

            val statusColor = when (item.status) {
                "menunggu" -> R.drawable.bg_status_keluhan
                "proses" -> R.drawable.bg_status_proses
                "selesai" -> R.drawable.bg_status_selesai
                else -> R.drawable.bg_status_keluhan
            }
            binding.tvStatus.setBackgroundResource(statusColor)
        }
    }
}