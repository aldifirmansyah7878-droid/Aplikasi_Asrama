package com.example.aplikasi_asrama.pembayaran

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.api.model.PembayaranData
import com.example.aplikasi_asrama.databinding.ItemPembayaranUserBinding
import java.text.NumberFormat
import java.util.Locale

class PembayaranUserAdapter(
    private val onUpload: (PembayaranData) -> Unit,
    private val onDetail: (PembayaranData) -> Unit
) : RecyclerView.Adapter<PembayaranUserAdapter.ViewHolder>() {

    private var items = listOf<PembayaranData>()

    fun submitList(list: List<PembayaranData>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPembayaranUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

        holder.binding.tvPeriode.text = item.bulanTahun
        holder.binding.tvJumlah.text = "Rp ${formatter.format(item.jumlah)}"
        holder.binding.tvMetode.text = "Metode: ${item.metode ?: "-"}"
        holder.binding.tvStatus.text = item.status

        when (item.status) {
            "Lunas" -> holder.binding.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
            "Ditolak" -> holder.binding.tvStatus.setBackgroundColor(Color.parseColor("#F44336"))
            else -> holder.binding.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
        }

        // Tombol upload hanya untuk status "Menunggu"
        holder.binding.btnUpload.visibility = if (item.status == "Menunggu") android.view.View.VISIBLE else android.view.View.GONE
        holder.binding.btnUpload.setOnClickListener { onUpload(item) }
        holder.binding.btnDetail.setOnClickListener { onDetail(item) }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(val binding: ItemPembayaranUserBinding) : RecyclerView.ViewHolder(binding.root)
}