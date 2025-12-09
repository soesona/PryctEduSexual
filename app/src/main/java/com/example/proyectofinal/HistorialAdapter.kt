package com.example.proyectofinal



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialAdapter(
    private val ciclos: List<CycleEntity>,
    private val onEditar: (CycleEntity) -> Unit,
    private val onEliminar: (CycleEntity) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    inner class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFechas: TextView = view.findViewById(R.id.tvFechas)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val ciclo = ciclos[position]
        holder.tvFechas.text = "Inicio: ${ciclo.startDate} • Fin: ${ciclo.endDate}"

        holder.itemView.setOnClickListener { onEditar(ciclo) }
        holder.btnEditar.setOnClickListener { onEditar(ciclo) }
        holder.btnEliminar.setOnClickListener { onEliminar(ciclo) }
    }

    override fun getItemCount() = ciclos.size
}
