package com.example.aplikasi_asrama.ui.izin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.IzinData
import com.example.aplikasi_asrama.databinding.ItemIzinBinding

class IzinAdminAdapter(
    private val onItemClick: (IzinData) -> Unit
) : RecyclerView.Adapter<IzinAdminAdapter.ViewHolder>() {

    private var items: List<IzinData> = emptyList()

    fun submitList(newList: List<IzinData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIzinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemIzinBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: IzinData) {
            binding.tvNamaPenghuni.text = item.namaPenghuni
            binding.tvNomorKamar.text = "Kamar: ${item.nomorKamar}"
            binding.tvTanggalKeluar.text = "Keluar: ${item.tanggalKeluar} ${item.jamKeluar}"

            // ===== STATUS TEXT =====
            val statusText = when (item.status) {
                "keluar" -> "Keluar"
                "kembali" -> "Kembali"
                "disetujui" -> "Disetujui"
                "ditolak" -> "Ditolak"
                else -> "Keluar"
            }
            binding.tvStatus.text = statusText

            // ===== WARNA STATUS =====
            val statusColor = when (item.status) {
                "keluar" -> R.drawable.bg_status_keluar
                "kembali" -> R.drawable.bg_status_kembali
                "disetujui" -> R.drawable.bg_status_disetujui
                "ditolak" -> R.drawable.bg_status_ditolak
                else -> R.drawable.bg_status_keluar
            }
            binding.tvStatus.setBackgroundResource(statusColor)
        }
    }
}