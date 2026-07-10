package com.example.aplikasi_asrama.tamu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.api.model.TamuData
import com.example.aplikasi_asrama.databinding.ItemTamuBinding

class TamuAdapter(private val onItemClick: (TamuData) -> Unit) : RecyclerView.Adapter<TamuAdapter.ViewHolder>() {
    private var list = listOf<TamuData>()

    fun submitList(newList: List<TamuData>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTamuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(private val binding: ItemTamuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tamu: TamuData) {
            binding.tvNamaTamu.text = tamu.namaTamu
            // Perbaikan: gunakan 'tujuanPenghuni' sesuai model API
            binding.tvTujuan.text = "Menemui: ${tamu.tujuanPenghuni}"
            binding.tvNomorKamar.text = "Kamar ${tamu.nomorKamar}"
            binding.tvWaktu.text = tamu.waktuKunjungan
            binding.tvHubungan.text = tamu.hubungan
            binding.tvStatus.text = tamu.status
            when (tamu.status) {
                "Menunggu" -> binding.tvStatus.setBackgroundColor(0xFFFF9800.toInt())
                "Sedang Berkunjung" -> binding.tvStatus.setBackgroundColor(0xFF2196F3.toInt())
                "Selesai" -> binding.tvStatus.setBackgroundColor(0xFF4CAF50.toInt())
            }
            binding.root.setOnClickListener { onItemClick(tamu) }
        }
    }
}