package com.example.proyectofinal

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
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

        actualizarPuntajeTotalEnMenu()
        mostrarVistaMenu()


        binding.btnJugarMetodos.setOnClickListener { iniciarPartida("metodos") }
        binding.btnJugarEts.setOnClickListener { iniciarPartida("ets") }
        binding.btnJugarMitos.setOnClickListener { iniciarPartida("mitos") }
        binding.btnJugarGeneral.setOnClickListener { iniciarPartida("general") }


        binding.btnVerdadero.setOnClickListener { procesarRespuesta(true) }
        binding.btnFalso.setOnClickListener { procesarRespuesta(false) }

        binding.btnSiguientePregunta.setOnClickListener {
            indiceActual++
            mostrarPregunta()
        }

        binding.btnSalirJuego.setOnClickListener { mostrarVistaMenu() }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutJuego.visibility == View.VISIBLE) {
                    mostrarVistaMenu()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })


        val categoriaExterna = arguments?.getString("categoria")
        if (categoriaExterna != null) {
            iniciarPartida(categoriaExterna)
            arguments?.clear()
        }
    }

    private fun mostrarVistaMenu() {
        binding.layoutJuego.visibility = View.GONE
        binding.layoutMenu.visibility = View.VISIBLE
        actualizarPuntajeTotalEnMenu()
        toggleBottomNav(true)
    }

    private fun iniciarPartida(categoria: String) {
        categoriaActual = categoria
        val bancoCompleto = obtenerBancoDePreguntas(categoria)


        listaPreguntas = bancoCompleto.shuffled().take(10)

        binding.layoutMenu.visibility = View.GONE
        binding.layoutJuego.visibility = View.VISIBLE
        binding.tvTituloCategoria.text = "Quiz: ${categoria.uppercase()}"

        indiceActual = 0
        puntajeSesion = 0
        mostrarPregunta()
        toggleBottomNav(false)
    }

    private fun mostrarPregunta() {
        if (indiceActual < listaPreguntas.size) {
            binding.tvPreguntaJuego.text = listaPreguntas[indiceActual].texto
            binding.progressBarQuiz.progress = ((indiceActual.toFloat() / listaPreguntas.size) * 100).toInt()

            binding.cvFeedback.visibility = View.GONE
            binding.btnVerdadero.isEnabled = true
            binding.btnFalso.isEnabled = true
            binding.btnVerdadero.alpha = 1.0f
            binding.btnFalso.alpha = 1.0f
        } else {
            finalizarJuego()
        }
    }

    private fun procesarRespuesta(respuestaUsuario: Boolean) {
        val p = listaPreguntas[indiceActual]
        val esCorrecto = (respuestaUsuario == p.esVerdadera)

        binding.btnVerdadero.isEnabled = false
        binding.btnFalso.isEnabled = false
        binding.btnVerdadero.alpha = 0.5f
        binding.btnFalso.alpha = 0.5f

        binding.cvFeedback.visibility = View.VISIBLE
        binding.tvFeedbackCuerpo.text = p.explicacion

        if (esCorrecto) {
            puntajeSesion += 10
            binding.tvFeedbackTitulo.text = "¡Correcto!"
            binding.tvFeedbackTitulo.setTextColor(Color.parseColor("#2E7D32"))
            binding.cvFeedback.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
        } else {
            binding.tvFeedbackTitulo.text = "Incorrecto"
            binding.tvFeedbackTitulo.setTextColor(Color.parseColor("#C62828"))
            binding.cvFeedback.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
        }
    }

    private fun finalizarJuego() {
        val sessionPrefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        val userId = sessionPrefs.getInt("USER_ID", -1)
        var mensaje = "Puntos obtenidos: $puntajeSesion / 100"

        if (userId != -1) {
            val prefs = requireActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
            val claveRecord = "record_${categoriaActual}_$userId"
            val recordActual = prefs.getInt(claveRecord, 0)

            if (puntajeSesion > recordActual) {
                prefs.edit().putInt(claveRecord, puntajeSesion).apply()
                mensaje = "🏆 ¡NUEVO RÉCORD!\nLograste $puntajeSesion puntos."
            }
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Fin del Quiz")
            .setMessage(mensaje)
            .setPositiveButton("Volver al Menú") { _, _ -> mostrarVistaMenu() }
            .setCancelable(false)
            .show()
    }

    private fun actualizarPuntajeTotalEnMenu() {
        val sessionPrefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        val userId = sessionPrefs.getInt("USER_ID", -1)

        if (userId != -1) {
            val prefs = requireActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
            val rMetodos = prefs.getInt("record_metodos_$userId", 0)
            val rEts = prefs.getInt("record_ets_$userId", 0)
            val rMitos = prefs.getInt("record_mitos_$userId", 0)
            val rGeneral = prefs.getInt("record_general_$userId", 0)

            val sumaTotal = rMetodos + rEts + rMitos + rGeneral
            binding.tvTotalScoreMenu.text = "$sumaTotal pts"


            binding.tvRecordMetodos?.text = "💊 Métodos: $rMetodos"
            binding.tvRecordEts?.text = "🦠 ETS: $rEts"
            binding.tvRecordMitos?.text = "🧠 Mitos: $rMitos"
            binding.tvRecordGeneral?.text = "🏆 General: $rGeneral"
        } else {
            binding.tvTotalScoreMenu.text = "0 pts"
        }
    }

    private fun toggleBottomNav(visible: Boolean) {
        val activity = requireActivity()
        val bottomNav = activity.findViewById<View>(R.id.bottom_nav_view)
        if (visible) bottomNav?.visibility = View.VISIBLE else bottomNav?.visibility = View.GONE
    }

    private fun obtenerBancoDePreguntas(categoria: String): List<Pregunta> {
        return when (categoria) {
            "metodos" -> listOf(
                Pregunta("¿La píldora anticonceptiva protege contra ETS?", false, "Solo previene embarazo."),
                Pregunta("¿El DIU puede durar más de 3 años?", true, "Sí, el de cobre dura hasta 10 años."),
                Pregunta("¿Es necesario receta para el condón masculino?", false, "Es de venta libre."),
                Pregunta("¿La vasectomía es efectiva inmediatamente?", false, "Requiere esperar unos meses."),
                Pregunta("¿El implante subdérmico va en la pierna?", false, "Se coloca en el brazo."),
                Pregunta("¿El método del ritmo (calendario) es 100% seguro?", false, "Falla mucho porque el ciclo varía."),
                Pregunta("¿Se puede usar condón masculino y femenino a la vez?", false, "Nunca. La fricción los rompe."),
                Pregunta("¿La pastilla del día después es un método regular?", false, "Es solo para emergencias."),
                Pregunta("¿El parche anticonceptivo se cambia cada día?", false, "Se cambia una vez a la semana."),
                Pregunta("¿La ligadura de trompas es permanente?", true, "Es esterilización permanente."),
                Pregunta("¿El anillo vaginal se deja puesto durante el sexo?", true, "Sí, se deja adentro 3 semanas."),
                Pregunta("¿La inyección anticonceptiva se pone cada año?", false, "Suele ser mensual o trimestral."),
                Pregunta("¿El condón tiene fecha de caducidad?", true, "Sí, y si caduca se rompe fácil."),
                Pregunta("¿Guardar el condón en la billetera es bueno?", false, "El calor y la fricción lo dañan."),
                Pregunta("¿Los espermicidas son muy efectivos solos?", false, "Deben usarse junto con otro método."),
                Pregunta("¿El DIU hormonal reduce el sangrado?", true, "Suele hacer las reglas más ligeras."),
                Pregunta("¿La vasectomía afecta la virilidad?", false, "No afecta las hormonas ni el deseo."),
                Pregunta("¿El implante se nota a simple vista?", false, "Es muy pequeño y va bajo la piel."),
                Pregunta("¿Si olvido la píldora un día, hay riesgo?", true, "Sí, reduce su efectividad."),
                Pregunta("¿El coito interrumpido protege de ETS?", false, "No protege de nada.")
            )
            "ets" -> listOf(
                Pregunta("¿El VIH se contagia por abrazos?", false, "Solo fluidos sexuales o sangre."),
                Pregunta("¿La sífilis se cura?", true, "Sí, con antibióticos."),
                Pregunta("¿El herpes tiene cura definitiva?", false, "El virus permanece en el cuerpo."),
                Pregunta("¿El VPH tiene vacuna?", true, "Sí, y es muy recomendada."),
                Pregunta("¿Las ETS siempre tienen síntomas?", false, "Muchas son silenciosas al inicio."),
                Pregunta("¿La gonorrea puede causar infertilidad?", true, "Si no se trata, daña los órganos."),
                Pregunta("¿El sexo oral puede transmitir ETS?", true, "Sí, como herpes o sífilis."),
                Pregunta("¿Si ya tuve sífilis, soy inmune?", false, "Te puedes volver a contagiar."),
                Pregunta("¿La Clamidia es una bacteria?", true, "Sí, y se cura con antibióticos."),
                Pregunta("¿Solo las personas promiscuas tienen ETS?", false, "Cualquiera sexualmente activo se expone."),
                Pregunta("¿El VIH es lo mismo que el SIDA?", false, "El SIDA es la etapa avanzada."),
                Pregunta("¿La hepatitis B se transmite sexualmente?", true, "Es una vía común."),
                Pregunta("¿Los piojos púbicos (ladillas) son una ETS?", true, "Se transmiten por contacto íntimo."),
                Pregunta("¿Hacerse duchas vaginales previene ETS?", false, "Aumenta el riesgo."),
                Pregunta("¿El condón protege 100% del VPH?", false, "Reduce riesgo, pero no cubre toda la piel."),
                Pregunta("¿Se puede tener VIH y verse sano?", true, "Sí, por muchos años."),
                Pregunta("¿La prueba de VIH duele?", false, "Es un simple análisis de sangre."),
                Pregunta("¿Todas las verrugas genitales son cáncer?", false, "La mayoría son benignas."),
                Pregunta("¿Besar transmite VIH?", false, "La saliva no transmite el virus."),
                Pregunta("¿Si mi pareja dice que está limpia, le creo?", false, "Sin pruebas recientes, no se puede saber.")
            )
            "mitos" -> listOf(
                Pregunta("¿Lavarse evita el embarazo?", false, "No sirve de nada."),
                Pregunta("¿La primera vez no embaraza?", false, "Mito peligroso, sí embaraza."),
                Pregunta("¿El coito interrumpido es seguro?", false, "El líquido preseminal tiene esperma."),
                Pregunta("¿El limón corta la regla?", false, "Falso."),
                Pregunta("¿Doble condón protege más?", false, "Falso, se rompen."),
                Pregunta("¿Sexo en el agua evita embarazo?", false, "No, el esperma entra igual."),
                Pregunta("¿Si la mujer está arriba no se embaraza?", false, "La gravedad no afecta."),
                Pregunta("¿Sin orgasmo no hay embarazo?", false, "Depende de la ovulación."),
                Pregunta("¿Antibióticos afectan la píldora?", true, "Algunos reducen su efecto."),
                Pregunta("¿Solo el hombre debe llevar condón?", false, "Es responsabilidad de ambos."),
                Pregunta("¿Sexo anal no embaraza?", true, "Cierto, pero transmite ETS."),
                Pregunta("¿La menstruación limpia el semen?", false, "No tiene relación."),
                Pregunta("¿Saltar después del sexo expulsa el semen?", false, "Falso, no funciona."),
                Pregunta("¿El alcohol mejora el sexo?", false, "Dificulta la erección y juicio."),
                Pregunta("¿La píldora engorda a todas?", false, "Depende de cada cuerpo."),
                Pregunta("¿Si no eyacula dentro no hay riesgo?", false, "El líquido preseminal es riesgoso."),
                Pregunta("¿Tener sexo durante la regla es seguro?", false, "Hay riesgo de embarazo y ETS."),
                Pregunta("¿La Coca-Cola sirve de espermicida?", false, "Mito absurdo y dañino."),
                Pregunta("¿La pastilla del día después aborta?", false, "Solo evita la ovulación."),
                Pregunta("¿Los tés de hierbas evitan embarazos?", false, "No tienen eficacia comprobada.")
            )

            else -> listOf(
                Pregunta("¿El consentimiento se puede retirar a la mitad?", true, "Si alguien dice NO, es NO."),
                Pregunta("¿Orinar después del sexo ayuda?", true, "Ayuda a prevenir infecciones urinarias."),
                Pregunta("¿La pornografía es educación sexual?", false, "Es ficción y no refleja la realidad."),
                Pregunta("¿Es normal que el sexo duela?", false, "No, el dolor indica falta de lubricación o problemas."),
                Pregunta("¿La masturbación causa infertilidad?", false, "Es un mito falso."),
                Pregunta("¿Los hombres deben ir al urólogo?", true, "Sí, para chequeos preventivos."),
                Pregunta("¿El clítoris solo sirve para el placer?", true, "Es su única función biológica conocida."),
                Pregunta("¿El himen es prueba de virginidad?", false, "No, se puede romper haciendo deporte."),
                Pregunta("¿Es necesario depilarse por higiene?", false, "El vello púbico protege de bacterias."),
                Pregunta("¿El tamaño importa para el placer?", false, "La técnica y la conexión importan más."),
                Pregunta("¿El ciclo menstrual dura siempre 28 días?", false, "Varía entre cada mujer."),
                Pregunta("¿El lubricante se puede usar con condón?", true, "Sí, pero debe ser a base de agua."),
                Pregunta("¿Solo las mujeres tienen suelo pélvico?", false, "Hombres y mujeres lo tienen."),
                Pregunta("¿La sexualidad termina en la vejez?", false, "Se puede disfrutar toda la vida."),
                Pregunta("¿Los juguetes sexuales son malos?", false, "Son herramientas saludables para explorar."),
                Pregunta("¿Es bueno hablar de sexo con la pareja?", true, "La comunicación mejora la relación."),
                Pregunta("¿Debo hacerme chequeos si tengo pareja estable?", true, "Es recomendable anualmente."),
                Pregunta("¿La educación sexual incita a tener sexo?", false, "Al contrario, fomenta decisiones responsables."),
                Pregunta("¿El sexo es solo penetración?", false, "Incluye muchas formas de intimidad."),
                Pregunta("¿Tengo derecho a decir NO siempre?", true, "Es tu derecho fundamental.")
            )
        }
    }
}