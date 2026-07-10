package com.example.aplikasi_asrama.penghuni

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.databinding.ItemPenghuniBinding

class PenghuniAdminAdapter(
    private val onEditClick: (PenghuniData) -> Unit,
    private val onDeleteClick: (PenghuniData) -> Unit
) : RecyclerView.Adapter<PenghuniAdminAdapter.ViewHolder>() {

    private var list = listOf<PenghuniData>()

    fun submitList(newList: List<PenghuniData>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPenghuniBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
        holder.binding.btnEdit.setOnClickListener { onEditClick(item) }
        holder.binding.btnHapus.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(val binding: ItemPenghuniBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PenghuniData) {
            binding.tvNama.text = data.nama ?: "-"
            binding.tvKamar.text = "Kamar: ${data.nomorKamar ?: "-"}"
            binding.tvNim.text = "NIM: ${data.Nim ?: "-"}"
            binding.tvNoTelp.text = "Telepon: ${data.noTelp ?: "-"}"
            binding.tvTanggalMasuk.text = "Tanggal Masuk: ${data.tanggalMasuk ?: "-"}"
            binding.tvAlamat.text = "Alamat: ${data.alamatAsal ?: "-"}"

            // Warna status
            when (data.status?.lowercase()) {
                "aktif" -> {
                    binding.tvStatus.text = "AKTIF"
                    binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                }
                else -> {
                    binding.tvStatus.text = "TIDAK AKTIF"
                    binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                }
            }
        }
    }
}