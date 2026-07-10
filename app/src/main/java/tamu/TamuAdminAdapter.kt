package com.example.aplikasi_asrama.tamu

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.model.TamuData
import com.example.aplikasi_asrama.databinding.ItemTamuAdminBinding

class TamuAdminAdapter(
    private val onStatusUpdate: (TamuData, String) -> Unit,
    private val onHapus: (TamuData) -> Unit
) : RecyclerView.Adapter<TamuAdminAdapter.ViewHolder>() {

    private var items: List<TamuData> = emptyList()

    fun submitList(newList: List<TamuData>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTamuAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]  // <-- PERBAIKAN: ambil item di sini

        holder.bind(item)

        // ===== KLIK ITEM UNTUK BUKA DETAIL =====
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailTamuActivity::class.java)
            intent.putExtra("TAMU_ID", item.id)
            holder.itemView.context.startActivity(intent)
        }

        // ===== TOMBOL AKSI =====
        holder.btnSetuju.setOnClickListener { onStatusUpdate(item, "disetujui") }
        holder.btnTolak.setOnClickListener { onStatusUpdate(item, "ditolak") }
        holder.btnSelesai.setOnClickListener { onStatusUpdate(item, "selesai") }
        holder.btnHapus.setOnClickListener { onHapus(item) }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemTamuAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val btnSetuju = binding.btnSetuju
        val btnTolak = binding.btnTolak
        val btnSelesai = binding.btnSelesai
        val btnHapus = binding.btnHapus

        fun bind(item: TamuData) {
            binding.tvNamaTamu.text = item.namaTamu
            binding.tvPenghuni.text = "Penghuni: ${item.tujuanPenghuni}"
            binding.tvKamar.text = "Kamar: ${item.nomorKamar}"
            binding.tvWaktu.text = "Waktu: ${item.waktuKunjungan}"

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

            // Tampilkan/sembunyikan tombol sesuai status
            when (item.status) {
                "menunggu" -> {
                    binding.btnSetuju.visibility = ViewGroup.VISIBLE
                    binding.btnTolak.visibility = ViewGroup.VISIBLE
                    binding.btnSelesai.visibility = ViewGroup.GONE
                }
                "disetujui" -> {
                    binding.btnSetuju.visibility = ViewGroup.GONE
                    binding.btnTolak.visibility = ViewGroup.GONE
                    binding.btnSelesai.visibility = ViewGroup.VISIBLE
                }
                else -> {
                    binding.btnSetuju.visibility = ViewGroup.GONE
                    binding.btnTolak.visibility = ViewGroup.GONE
                    binding.btnSelesai.visibility = ViewGroup.GONE
                }
            }
        }
    }
}