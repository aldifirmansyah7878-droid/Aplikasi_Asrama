package com.example.aplikasi_asrama.keluhan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.KeluhanData
import com.example.aplikasi_asrama.databinding.ItemKeluhanAdminBinding

class KeluhanAdminAdapter(
    private val onDetail: (KeluhanData) -> Unit,
    private val onTanggapan: (KeluhanData) -> Unit,
    private val onHapus: (KeluhanData) -> Unit
) : RecyclerView.Adapter<KeluhanAdminAdapter.ViewHolder>() {

    private var items: List<KeluhanData> = emptyList()

    fun submitList(newList: List<KeluhanData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKeluhanAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemKeluhanAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KeluhanData) {
            binding.tvNama.text = item.namaPenghuni
            binding.tvKamar.text = "Kamar: ${item.nomorKamar}"
            binding.tvJudul.text = item.judulKeluhan
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

            binding.btnDetail.setOnClickListener { onDetail(item) }
            binding.btnTanggapan.setOnClickListener { onTanggapan(item) }
            binding.btnHapus.setOnClickListener { onHapus(item) }
        }
    }
}