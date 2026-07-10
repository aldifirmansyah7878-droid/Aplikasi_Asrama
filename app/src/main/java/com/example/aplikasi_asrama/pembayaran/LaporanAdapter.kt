package com.example.aplikasi_asrama.laporan

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.api.model.PembayaranData
import com.example.aplikasi_asrama.databinding.ItemLaporanBinding
import java.text.NumberFormat
import java.util.Locale

class LaporanAdapter : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {

    private var items = listOf<PembayaranData>()

    fun submitList(list: List<PembayaranData>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLaporanBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvPenghuni.text = item.namaPenghuni ?: "Penghuni"
        holder.binding.tvPeriode.text = item.bulanTahun
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.binding.tvJumlah.text = "Rp ${formatter.format(item.jumlah)}"
        holder.binding.tvStatus.text = item.status
        val bgColor = when (item.status) {
            "Lunas" -> Color.parseColor("#4CAF50")
            "Ditolak" -> Color.parseColor("#F44336")
            else -> Color.parseColor("#FF9800")
        }
        holder.binding.tvStatus.setBackgroundColor(bgColor)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(val binding: ItemLaporanBinding) : RecyclerView.ViewHolder(binding.root)
}