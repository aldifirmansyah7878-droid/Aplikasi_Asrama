package com.example.aplikasi_asrama.izin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.IzinData
import com.example.aplikasi_asrama.databinding.ItemIzinBinding

class RiwayatIzinAdapter(
    private val onItemClick: (IzinData) -> Unit
) : RecyclerView.Adapter<RiwayatIzinAdapter.ViewHolder>() {

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

    inner class ViewHolder(private val binding: ItemIzinBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IzinData) {
            binding.tvNamaPenghuni.text = item.namaPenghuni
            binding.tvNomorKamar.text = "Kamar: ${item.nomorKamar}"
            binding.tvStatus.text = item.status
            binding.tvTanggalKeluar.text = "Keluar: ${item.tanggalKeluar} ${item.jamKeluar}"
            when (item.status) {
                "keluar" -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_keluar)
                "kembali" -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_kembali)
            }
        }
    }
}