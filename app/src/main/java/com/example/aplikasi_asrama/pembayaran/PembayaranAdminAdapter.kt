package com.example.aplikasi_asrama.pembayaran

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.aplikasi_asrama.api.RetrofitClient
import com.example.aplikasi_asrama.api.model.PembayaranData
import com.example.aplikasi_asrama.databinding.ItemPembayaranAdminBinding
import java.text.NumberFormat
import java.util.Locale

class PembayaranAdminAdapter(
    private val onVerifikasi: (PembayaranData) -> Unit,
    private val onTolak: (PembayaranData) -> Unit
) : RecyclerView.Adapter<PembayaranAdminAdapter.ViewHolder>() {

    private var items = listOf<PembayaranData>()

    fun submitList(list: List<PembayaranData>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPembayaranAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

        holder.binding.tvPenghuni.text = "${item.namaPenghuni ?: "-"} (Kamar ${item.nomorKamar ?: "-"})"
        holder.binding.tvPeriode.text = item.bulanTahun
        holder.binding.tvJumlah.text = "Rp ${formatter.format(item.jumlah)}"
        holder.binding.tvMetode.text = item.metode ?: "-"

        // Status badge
        holder.binding.tvStatus.text = item.status
        when (item.status) {
            "Lunas" -> holder.binding.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
            "Ditolak" -> holder.binding.tvStatus.setBackgroundColor(Color.parseColor("#F44336"))
            else -> holder.binding.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
        }

        // ========== TAMPILKAN BUKTI ==========
        val buktiUrl = item.buktiUrl
        if (!buktiUrl.isNullOrBlank()) {
            holder.binding.ivBukti.visibility = View.VISIBLE
            // Gunakan base URL + path (pastikan RetrofitClient.BASE_URL tersedia)
            val fullUrl = RetrofitClient.BASE_URL_IMAGE + buktiUrl
            holder.binding.ivBukti.load(fullUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }
            // Klik untuk zoom (opsional)
            holder.binding.ivBukti.setOnClickListener {
                // Bisa buka dialog fullscreen
            }
        } else {
            holder.binding.ivBukti.visibility = View.GONE
        }

        // Sembunyikan tombol aksi jika status sudah Lunas atau Ditolak
        if (item.status == "Lunas" || item.status == "Ditolak") {
            holder.binding.btnVerifikasi.visibility = View.GONE
            holder.binding.btnTolak.visibility = View.GONE
        } else {
            holder.binding.btnVerifikasi.visibility = View.VISIBLE
            holder.binding.btnTolak.visibility = View.VISIBLE
            holder.binding.btnVerifikasi.setOnClickListener { onVerifikasi(item) }
            holder.binding.btnTolak.setOnClickListener { onTolak(item) }
        }

        // Catatan admin
        if (!item.catatanAdmin.isNullOrBlank()) {
            holder.binding.tvCatatan.visibility = View.VISIBLE
            holder.binding.tvCatatan.text = "Catatan: ${item.catatanAdmin}"
        } else {
            holder.binding.tvCatatan.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(val binding: ItemPembayaranAdminBinding) : RecyclerView.ViewHolder(binding.root)
}