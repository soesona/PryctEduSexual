package com.example.proyectofinal

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.databinding.FragmentTrackerBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import java.util.*
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import java.time.format.TextStyle
import java.util.Locale
import androidx.core.view.children
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendar.core.DayPosition

class MonthViewContainer(view: View) : ViewContainer(view) {
    val titlesContainer = view as ViewGroup
}

class TrackerFragment : Fragment(R.layout.fragment_tracker) {

    private var _binding: FragmentTrackerBinding? = null
    private val binding get() = _binding!!


    private var fechaInicioCiclo: LocalDate? = null
    private var diasSeleccionados = mutableSetOf<LocalDate>()
    private var ciclosGuardados: List<CycleEntity> = emptyList()
    private var cicloSeleccionadoParaEditar: CycleEntity? = null


    private var userIdActual = -1
    private val duracionPeriodoDias = 5
    private val duracionCicloDias = 28

    private var currentPage = 0
    private val pageSize = 3



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrackerBinding.bind(view)


        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val daysOfWeek = daysOfWeek()

        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)


        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    val monthTitle = container.titlesContainer.findViewById<TextView>(R.id.monthTitle)
                    val weekDaysRow = container.titlesContainer.findViewById<ViewGroup>(R.id.weekDaysRow)

                    monthTitle?.text = data.yearMonth.month.getDisplayName(
                        TextStyle.FULL, Locale("es", "ES")
                    ) + " " + data.yearMonth.year

                    weekDaysRow?.children
                        ?.filterIsInstance<TextView>()
                        ?.forEachIndexed { index, textView ->
                            val dow = daysOfWeek[index]
                            textView.text = dow.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                        }
                }
            }


        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarDayText)
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {



                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                if (data.position != DayPosition.MonthDate) {
                    textView.visibility = View.INVISIBLE
                    return
                } else {
                    textView.visibility = View.VISIBLE
                }

                container.textView.text = data.date.dayOfMonth.toString()

                val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")



                if (cicloSeleccionadoParaEditar != null && diasSeleccionados.contains(data.date)) {
                    container.textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple))
                }

                else if (cicloSeleccionadoParaEditar == null && diasSeleccionados.contains(data.date)) {
                    container.textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.menstrualDays2))
                }

                else {
                    var pintado = false
                    for (ciclo in ciclosGuardados) {
                        val inicio = LocalDate.parse(ciclo.startDate, fmt)
                        val fin = LocalDate.parse(ciclo.endDate, fmt)
                        if (data.date in inicio..fin) {
                            container.textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.menstrualDays))
                            pintado = true
                            break
                        }
                    }
                    if (!pintado) {
                        container.textView.setBackgroundColor(Color.TRANSPARENT)
                    }
                }



                container.textView.setOnClickListener {
                    val fecha = data.date
                    if (fechaInicioCiclo == null) {
                        fechaInicioCiclo = fecha
                        diasSeleccionados.clear()
                        repeat(duracionPeriodoDias) {
                            diasSeleccionados.add(fecha.plusDays(it.toLong()))
                        }
                        cicloSeleccionadoParaEditar = null
                    } else {
                        if (diasSeleccionados.contains(fecha)) {
                            diasSeleccionados.remove(fecha)
                        } else {
                            diasSeleccionados.add(fecha)
                        }
                    }

                    val fmtTxt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    binding.tvInicio.text = fechaInicioCiclo?.format(fmtTxt) ?: "--/--/--"
                    binding.tvFin.text = diasSeleccionados.maxOrNull()?.format(fmtTxt) ?: "--/--/--"

                    binding.calendarView.notifyCalendarChanged()
                }
            }
        }


        val sessionPrefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        userIdActual = sessionPrefs.getInt("USER_ID", -1)

        if (userIdActual != -1) {
            val prefsDatos = requireActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
            val claveFechaUsuario = "Ultimo_periodo_$userIdActual"
            val ultimaFechaGuardada = prefsDatos.getLong(claveFechaUsuario, 0L)

            val baseMs = if (ultimaFechaGuardada != 0L) ultimaFechaGuardada else System.currentTimeMillis()
            actualizarTextos(baseMs)
        }


        binding.btnLogPeriodStart.setOnClickListener {
            if (fechaInicioCiclo != null && diasSeleccionados.isNotEmpty()) {
                val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val ciclo = CycleEntity(
                    userId = userIdActual,
                    startDate = fechaInicioCiclo!!.format(fmt),
                    endDate = diasSeleccionados.maxOrNull()!!.format(fmt)
                )
                guardarCiclo(ciclo)
            } else {
                Toast.makeText(requireContext(), "Selecciona un inicio y días del ciclo", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                mostrarPagina()
            }
        }

        binding.btnNextPage.setOnClickListener {
            val totalPages = (ciclosGuardados.size + pageSize - 1) / pageSize
            if (currentPage < totalPages - 1) {
                currentPage++
                mostrarPagina()
            }
        }

        cargarHistorialDesdeBD()
    }

    private fun actualizarTextos(fechaMs: Long) {
        val locale = Locale("es", "ES")
        val corto = SimpleDateFormat("dd/MM/yyyy", locale)
        val largo = SimpleDateFormat("EEEE d 'de' MMMM", locale)

        val calInicio = Calendar.getInstance().apply { timeInMillis = fechaMs }
        binding.tvInicio.text = corto.format(calInicio.time)

        val calFin = Calendar.getInstance().apply { timeInMillis = fechaMs; add(Calendar.DAY_OF_YEAR, duracionPeriodoDias) }
        binding.tvFin.text = corto.format(calFin.time)

        val calProx = Calendar.getInstance().apply { timeInMillis = fechaMs; add(Calendar.DAY_OF_YEAR, duracionCicloDias) }
        val textoProx = largo.format(calProx.time)
        binding.tvProximo.text = textoProx.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    private fun guardarCiclo(ciclo: CycleEntity) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val inicioNuevo = LocalDate.parse(ciclo.startDate, fmt)
            val finNuevo = LocalDate.parse(ciclo.endDate, fmt)

            var hayConflicto = false
            for (existente in ciclosGuardados) {
                val inicioExistente = LocalDate.parse(existente.startDate, fmt)
                val finExistente = LocalDate.parse(existente.endDate, fmt)


                if (cicloSeleccionadoParaEditar != null && existente.id == cicloSeleccionadoParaEditar!!.id) {
                    continue
                }

                if (inicioNuevo <= finExistente && finNuevo >= inicioExistente) {
                    hayConflicto = true
                    break
                }
            }

            if (hayConflicto) {
                Toast.makeText(
                    requireContext(),
                    "Ya existe un ciclo en ese rango. No se puede sobrescribir.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                cicloSeleccionadoParaEditar?.let { cicloAnterior ->
                    db.appDao().deleteCycle(cicloAnterior)
                    cicloSeleccionadoParaEditar = null
                }

                db.appDao().insertCycle(ciclo)

                Toast.makeText(
                    requireContext(),
                    "Periodo guardado: ${ciclo.startDate} - ${ciclo.endDate}",
                    Toast.LENGTH_SHORT
                ).show()

                fechaInicioCiclo = null
                diasSeleccionados.clear()
                binding.tvInicio.text = "--/--/--"
                binding.tvFin.text = "--/--/--"

                cargarHistorialDesdeBD()
                binding.calendarView.notifyCalendarChanged()
            }
        }
    }


    private fun mostrarPagina() {
        val totalPages = (ciclosGuardados.size + pageSize - 1) / pageSize
        val startIndex = currentPage * pageSize
        val endIndex = minOf(startIndex + pageSize, ciclosGuardados.size)

        val pagina = ciclosGuardados.subList(startIndex, endIndex)

        val adapter = HistorialAdapter(
            pagina,
            onEditar = { ciclo ->

                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val inicio = LocalDate.parse(ciclo.startDate, formatter)
                val fin = LocalDate.parse(ciclo.endDate, formatter)

                fechaInicioCiclo = inicio
                diasSeleccionados.clear()
                var fecha = inicio
                while (!fecha.isAfter(fin)) {
                    diasSeleccionados.add(fecha)
                    fecha = fecha.plusDays(1)
                }

                cicloSeleccionadoParaEditar = ciclo
                binding.tvInicio.text = ciclo.startDate
                binding.tvFin.text = ciclo.endDate

                binding.calendarView.scrollToMonth(YearMonth.from(inicio))
                binding.calendarView.notifyCalendarChanged()
            },
            onEliminar = { ciclo ->
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.appDao().deleteCycle(ciclo)
                    Toast.makeText(requireContext(), "Ciclo eliminado", Toast.LENGTH_SHORT).show()


                    cicloSeleccionadoParaEditar = null
                    diasSeleccionados.clear()
                    fechaInicioCiclo = null
                    binding.tvInicio.text = "--/--/--"
                    binding.tvFin.text = "--/--/--"

                    cargarHistorialDesdeBD()
                    binding.calendarView.notifyCalendarChanged()
                }
            }
        )

        binding.containerHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.containerHistorial.adapter = adapter

        binding.tvPageIndicator.text = "Página ${currentPage + 1} de $totalPages"
        binding.btnPrevPage.visibility =
            if (currentPage > 0) View.VISIBLE else View.GONE

        binding.btnNextPage.visibility =
            if (currentPage < totalPages - 1) View.VISIBLE else View.GONE
    }

    private fun cargarHistorialDesdeBD() {
        if (userIdActual == -1) return
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")


            ciclosGuardados = db.appDao().getHistory(userIdActual)
                .sortedByDescending { LocalDate.parse(it.startDate, formatter) }

            currentPage = 0
            mostrarPagina()


            binding.calendarView.notifyCalendarChanged()
        }
    }



        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
