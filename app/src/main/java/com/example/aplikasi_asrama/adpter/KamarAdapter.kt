package com.example.aplikasi_asrama.kamar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.KamarData
import com.example.aplikasi_asrama.databinding.ItemKamarBinding
import java.text.NumberFormat
import java.util.Locale

class KamarAdapter(
    private val onEdit: (KamarData) -> Unit,
    private val onDelete: (KamarData) -> Unit
) : ListAdapter<KamarData, KamarAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKamarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemKamarBinding,
        private val onEdit: (KamarData) -> Unit,
        private val onDelete: (KamarData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kamar: KamarData) {
            binding.tvNomorKamar.text = "Kamar ${kamar.nomorKamar}"
            binding.tvLantai.text = "Lantai ${kamar.lantai}"
            // Aman: jika terisi tidak ada, tampilkan 0
            val terisi = try { kamar.terisi } catch (e: Exception) { 0 }
            binding.tvKapasitas.text = "Kapasitas: $terisi/${kamar.kapasitas}"
            binding.tvHarga.text = formatRupiah(kamar.hargaPerBulan)
            binding.tvFasilitas.text = kamar.fasilitas ?: "-"
            binding.chipStatus.text = kamar.status
            binding.btnEdit.setOnClickListener { onEdit(kamar) }
            binding.btnDelete.setOnClickListener { onDelete(kamar) }
        }

        private fun formatRupiah(amount: Int): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(amount) + "/bulan"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<KamarData>() {
        override fun areItemsTheSame(oldItem: KamarData, newItem: KamarData) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: KamarData, newItem: KamarData) =
            oldItem == newItem
    }
}