package com.example.aplikasi_asrama.pembayaran

import com.example.aplikasi_asrama.model.Pembayaran
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.databinding.ItemPembayaranBinding
import java.text.NumberFormat
import java.util.Locale

class PembayaranAdapter(
    private val onDelete: (Pembayaran) -> Unit
) : ListAdapter<Pembayaran, PembayaranAdapter.VH>(Diff()) {

    inner class VH(val b: ItemPembayaranBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Pembayaran) {
            val fmt = NumberFormat.getCurrencyInstance(Locale("id","ID"))
            b.tvNama.text    = p.namaPenghuni
            b.tvKamar.text   = "Kamar " + p.nomorKamar
            b.tvBulan.text   = p.bulanTahun
            b.tvJumlah.text  = fmt.format(p.jumlah)
            b.tvTanggal.text = "Dibayar: " + p.tanggalBayar
            b.tvStatus.text  = p.status
            b.btnDelete.setOnClickListener {
                AlertDialog.Builder(b.root.context)
                    .setTitle("Hapus Pembayaran")
                    .setMessage("Hapus data pembayaran ini?")
                    .setPositiveButton("Hapus") { _, _ -> onDelete(p) }
                    .setNegativeButton("Batal", null).show()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPembayaranBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos))

    class Diff : DiffUtil.ItemCallback<Pembayaran>() {
        override fun areItemsTheSame(a: Pembayaran, b: Pembayaran) = a.id == b.id
        override fun areContentsTheSame(a: Pembayaran, b: Pembayaran) = a == b
    }
}