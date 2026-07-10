package com.example.aplikasi_asrama.penghuni

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.databinding.ActivityPenghuniBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import kotlin.collections.filter

class PenghuniActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPenghuniBinding
    private lateinit var adapter: PenghuniAdapter
    private var allPenghuni = listOf<PenghuniData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenghuniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Data Penghuni"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnTambah.setOnClickListener {
            startActivity(Intent(this, TambahEditPenghuniActivity::class.java))
        }

        adapter = PenghuniAdapter { penghuni, action ->
            when (action) {
                "edit" -> {
                    val intent = Intent(this, TambahEditPenghuniActivity::class.java)
                    intent.putExtra("penghuni_id", penghuni.id)
                    startActivity(intent)
                }
                "hapus" -> hapusPenghuni(penghuni)
            }
        }
        binding.rvPenghuni.layoutManager = LinearLayoutManager(this)
        binding.rvPenghuni.adapter = adapter

        binding.chipSemua.setOnClickListener { loadData(null) }
        binding.chipAktif.setOnClickListener { loadData("Aktif") }
        binding.chipTidakAktif.setOnClickListener { loadData("Tidak Aktif") }

        loadData(null)
    }

    private fun loadData(filterStatus: String?) {
        lifecycleScope.launch {
            val repository = ApiRepository()
            val data = repository.getAllPenghuni()
            allPenghuni = if (filterStatus == null) data else data.filter {
                it.status?.equals(filterStatus, ignoreCase = true) == true
            }
            adapter.submitList(allPenghuni)
        }
    }

    private fun hapusPenghuni(penghuni: PenghuniData) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Penghuni")
            .setMessage("Yakin ingin menghapus ${penghuni.nama}?")
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    val success = ApiRepository().hapusPenghuni(penghuni.id)
                    if (success) {
                        loadData(null)
                    } else {
                        android.widget.Toast.makeText(this@PenghuniActivity, "Gagal hapus", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData(null)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // ==================== ADAPTER ====================
    class PenghuniAdapter(private val onAction: (PenghuniData, String) -> Unit) : RecyclerView.Adapter<PenghuniAdapter.ViewHolder>() {
        private var items = listOf<PenghuniData>()

        fun submitList(list: List<PenghuniData>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_penghuni, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val p = items[position]
            // Gunakan safe call dan default value untuk menghindari null
            holder.tvNama.text = p.nama ?: "-"
            holder.tvKamar.text = p.nomorKamar?.ifEmpty { "-" } ?: "-"
            holder.tvNim.text = p.Nim?.ifEmpty { "-" } ?: "-"
            holder.tvNoTelp.text = p.noTelp?.ifEmpty { "-" } ?: "-"
            holder.tvTanggalMasuk.text = p.tanggalMasuk?.ifEmpty { "-" } ?: "-"
            holder.tvAlamat.text = p.alamatAsal?.ifEmpty { "-" } ?: "-"
            val status = p.status ?: "Tidak Aktif"
            holder.tvStatus.text = status
            // Warna status
            if (status.equals("Aktif", ignoreCase = true)) {
                holder.tvStatus.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            } else {
                holder.tvStatus.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            }
            holder.btnEdit.setOnClickListener { onAction(p, "edit") }
            holder.btnHapus.setOnClickListener { onAction(p, "hapus") }
        }

        override fun getItemCount() = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvKamar: TextView = itemView.findViewById(R.id.tvKamar)
            val tvNim: TextView = itemView.findViewById(R.id.tvNim)
            val tvNoTelp: TextView = itemView.findViewById(R.id.tvNoTelp)
            val tvTanggalMasuk: TextView = itemView.findViewById(R.id.tvTanggalMasuk)
            val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamat)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
            val btnHapus: Button = itemView.findViewById(R.id.btnHapus)
        }
    }
}