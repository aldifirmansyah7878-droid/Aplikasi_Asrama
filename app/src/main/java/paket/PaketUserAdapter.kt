package com.example.aplikasi_asrama.paket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.PaketData
import com.example.aplikasi_asrama.databinding.ItemPaketUserBinding

class PaketUserAdapter(
    private val onItemClick: (PaketData) -> Unit
) : RecyclerView.Adapter<PaketUserAdapter.ViewHolder>() {

    private var items: List<PaketData> = emptyList()

    fun submitList(newList: List<PaketData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaketUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener { onItemClick(items[position]) }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemPaketUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaketData) {
            binding.tvNamaPengirim.text = item.namaPengirim
            binding.tvJenisPaket.text = "Jenis: ${item.jenisPaket}"
            binding.tvTanggalDatang.text = "Tanggal: ${item.tanggalDatang}"

            val statusText = when (item.status) {
                "menunggu" -> "Menunggu"
                "diambil" -> "Diambil"
                "selesai" -> "Selesai"
                else -> "Menunggu"
            }
            binding.tvStatus.text = statusText

            val statusColor = when (item.status) {
                "menunggu" -> R.drawable.bg_status_paket_menunggu
                "diambil" -> R.drawable.bg_status_paket_diambil
                "selesai" -> R.drawable.bg_status_paket_selesai
                else -> R.drawable.bg_status_paket_menunggu
            }
            binding.tvStatus.setBackgroundResource(statusColor)
        }
    }
}