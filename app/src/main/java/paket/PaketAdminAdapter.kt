package com.example.aplikasi_asrama.paket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.PaketData
import com.example.aplikasi_asrama.databinding.ItemPaketAdminBinding

class PaketAdminAdapter(
    private val onDetail: (PaketData) -> Unit,
    private val onStatusUpdate: (PaketData, String) -> Unit
) : RecyclerView.Adapter<PaketAdminAdapter.ViewHolder>() {

    private var items: List<PaketData> = emptyList()

    fun submitList(newList: List<PaketData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaketAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemPaketAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaketData) {
            binding.tvNamaPenghuni.text = item.namaPenghuni
            binding.tvKamar.text = "Kamar: ${item.nomorKamar}"
            binding.tvPengirim.text = item.namaPengirim
            binding.tvJenisPaket.text = item.jenisPaket
            binding.tvTanggal.text = item.tanggalDatang

            // ===== STATUS & WARNA =====
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

            // ===== TOMBOL AKSI SESUAI STATUS =====
            when (item.status) {
                "menunggu" -> {
                    // Status menunggu: tampilkan Terima & Tolak, sembunyikan Selesai
                    binding.btnTerima.visibility = ViewGroup.VISIBLE
                    binding.btnTolak.visibility = ViewGroup.VISIBLE
                    binding.btnSelesai.visibility = ViewGroup.GONE
                }
                "diambil" -> {
                    // Status diambil: sembunyikan Terima & Tolak, tampilkan Selesai
                    binding.btnTerima.visibility = ViewGroup.GONE
                    binding.btnTolak.visibility = ViewGroup.GONE
                    binding.btnSelesai.visibility = ViewGroup.VISIBLE
                }
                "selesai" -> {
                    // Status selesai: sembunyikan semua tombol aksi
                    binding.btnTerima.visibility = ViewGroup.GONE
                    binding.btnTolak.visibility = ViewGroup.GONE
                    binding.btnSelesai.visibility = ViewGroup.GONE
                }
                else -> {
                    binding.btnTerima.visibility = ViewGroup.GONE
                    binding.btnTolak.visibility = ViewGroup.GONE
                    binding.btnSelesai.visibility = ViewGroup.GONE
                }
            }

            // Tombol Detail selalu tampil
            binding.btnDetail.visibility = ViewGroup.VISIBLE

            // ===== CLICK LISTENER =====
            binding.btnDetail.setOnClickListener { onDetail(item) }
            binding.btnTerima.setOnClickListener { onStatusUpdate(item, "diambil") }
            binding.btnTolak.setOnClickListener { onStatusUpdate(item, "selesai") }
            binding.btnSelesai.setOnClickListener { onStatusUpdate(item, "selesai") }
        }
    }
}