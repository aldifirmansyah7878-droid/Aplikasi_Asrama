package com.example.aplikasi_asrama.tamu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.TamuData
import com.example.aplikasi_asrama.databinding.ItemTamuUserBinding

class TamuUserAdapter(
    private val onItemClick: (TamuData) -> Unit  // <-- click listener
) : RecyclerView.Adapter<TamuUserAdapter.ViewHolder>() {

    private var items: List<TamuData> = emptyList()

    fun submitList(newList: List<TamuData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTamuUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        // ===== KLIK ITEM UNTUK BUKA DETAIL =====
        holder.itemView.setOnClickListener {
            onItemClick(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemTamuUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TamuData) {
            binding.tvNamaTamu.text = item.namaTamu
            binding.tvHubungan.text = "Hubungan: ${item.hubungan}"
            binding.tvTujuan.text = "Tujuan: ${item.tujuanPenghuni}"
            binding.tvWaktu.text = "Waktu: ${item.waktuKunjungan}"
            binding.tvKamar.text = "Kamar: ${item.nomorKamar}"

            val statusText = when (item.status) {
                "menunggu" -> "Menunggu"
                "disetujui" -> "Disetujui"
                "ditolak" -> "Ditolak"
                "selesai" -> "Selesai"
                else -> "Menunggu"
            }
            binding.tvStatus.text = statusText

            val statusColor = when (item.status) {
                "menunggu" -> R.drawable.bg_status_tamu_menunggu
                "disetujui" -> R.drawable.bg_status_tamu_disetujui
                "ditolak" -> R.drawable.bg_status_tamu_ditolak
                "selesai" -> R.drawable.bg_status_tamu_selesai
                else -> R.drawable.bg_status_tamu_menunggu
            }
            binding.tvStatus.setBackgroundResource(statusColor)
        }
    }
}