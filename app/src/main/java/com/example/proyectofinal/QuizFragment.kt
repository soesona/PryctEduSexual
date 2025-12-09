package com.example.proyectofinal

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinal.databinding.FragmentQuizBinding

data class Pregunta(val texto: String, val esVerdadera: Boolean, val explicacion: String)

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private lateinit var binding: FragmentQuizBinding
    private var indiceActual = 0
    private var puntajeSesion = 0
    private var categoriaActual = "general"
    private var listaPreguntas = listOf<Pregunta>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentQuizBinding.bind(view)

        categoriaActual = arguments?.getString("categoria") ?: "general"
        cargarBancoPreguntas(categoriaActual)

        binding.tvTituloQuiz.text = "Quiz: ${categoriaActual.uppercase()}"
        reiniciarJuego()

        binding.btnVerdadero.setOnClickListener { verificar(true) }
        binding.btnFalso.setOnClickListener { verificar(false) }
    }

    private fun cargarBancoPreguntas(categoria: String) {

        when (categoria) {
            "metodos" -> listaPreguntas = listOf(
                Pregunta("¿La píldora anticonceptiva protege contra ETS?", false, "Solo previene embarazo, no infecciones."),
                Pregunta("¿El DIU puede durar más de 3 años?", true, "Sí, algunos duran de 3 a 10 años dependiendo el tipo."),
                Pregunta("¿Es necesario receta para el condón masculino?", false, "Es de venta libre en farmacias y supermercados."),
                Pregunta("¿La vasectomía es efectiva inmediatamente?", false, "No, se requiere esperar unos meses y hacer un conteo de esperma."),
                Pregunta("¿El implante subdérmico va en la pierna?", false, "Se coloca en el brazo.")
            )
            "ets" -> listaPreguntas = listOf(
                Pregunta("¿El VIH se puede transmitir por compartir vasos?", false, "Solo por fluidos sexuales, sangre o leche materna."),
                Pregunta("¿La sífilis tiene cura?", true, "Sí, con antibióticos si se detecta a tiempo."),
                Pregunta("¿El herpes genital desaparece para siempre?", false, "El virus permanece en el cuerpo, aunque las llagas sanen."),
                Pregunta("¿El condón protege 100% contra el VPH?", false, "Reduce el riesgo, pero el VPH puede estar en zonas que el condón no cubre."),
                Pregunta("¿Se puede tener una ETS sin síntomas?", true, "Muchas ETS son asintomáticas al inicio.")
            )
            "mitos" -> listaPreguntas = listOf(
                Pregunta("¿Lavarse después del sexo evita el embarazo?", false, "Los espermatozoides son muy rápidos, el lavado no los alcanza."),
                Pregunta("¿La primera vez no te puedes embarazar?", false, "Falso. La probabilidad es la misma siempre."),
                Pregunta("¿Venirse afuera es un método seguro?", false, "El líquido preseminal puede embarazar."),
                Pregunta("¿Tomar limón corta la menstruación o el embarazo?", false, "Mito absoluto. No tiene efecto biológico en el útero."),
                Pregunta("¿La pastilla del día después es abortiva?", false, "No, evita que el óvulo salga, no detiene un embarazo ya implantado.")
            )
            else -> listaPreguntas = listOf(Pregunta("¿Consentimiento es clave?", true, "Siempre."))
        }
    }

    private fun reiniciarJuego() {
        indiceActual = 0
        puntajeSesion = 0
        mostrarPregunta()
    }

    private fun mostrarPregunta() {
        if (indiceActual < listaPreguntas.size) {
            binding.tvPregunta.text = listaPreguntas[indiceActual].texto
            binding.progressBarQuiz.progress = ((indiceActual.toFloat() / listaPreguntas.size) * 100).toInt()
        } else {
            finalizarJuego()
        }
    }

    private fun verificar(respuestaUsuario: Boolean) {
        val p = listaPreguntas[indiceActual]
        if (respuestaUsuario == p.esVerdadera) {
            puntajeSesion += 10
            Toast.makeText(context, "✅ Correcto", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ Incorrecto: ${p.explicacion}", Toast.LENGTH_SHORT).show()
        }
        indiceActual++
        mostrarPregunta()
    }

    // --- AQUÍ ESTÁ LA CORRECCIÓN IMPORTANTE ---
    private fun finalizarJuego() {
        // 1. Obtener ID del Usuario Actual
        val sessionPrefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        val userId = sessionPrefs.getInt("USER_ID", -1)

        if (userId != -1) {
            val prefs = requireActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE)

            // 2. Claves ÚNICAS por usuario (Agregamos _$userId al final)
            val claveRecord = "record_${categoriaActual}_$userId"
            val claveGlobal = "puntaje_global_$userId"

            val recordActual = prefs.getInt(claveRecord, 0)


            if (puntajeSesion > recordActual) {

                prefs.edit().putInt(claveRecord, puntajeSesion).apply()


                val totalGlobal = prefs.getInt(claveGlobal, 0) + puntajeSesion
                prefs.edit().putInt(claveGlobal, totalGlobal).apply()

                mostrarDialogo("🏆 ¡NUEVO RÉCORD!", "Has superado tu puntaje anterior.\nPuntos: $puntajeSesion")
            } else {
                mostrarDialogo("Fin del Quiz", "Puntos: $puntajeSesion\nTu récord es: $recordActual")
            }
        } else {
            mostrarDialogo("Fin del Quiz", "Puntos: $puntajeSesion (Inicia sesión para guardar)")
        }
    }

    private fun mostrarDialogo(titulo: String, mensaje: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Volver al Menú") { _, _ ->
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }
}