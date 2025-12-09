package com.example.proyectofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import android.app.DatePickerDialog // Necesario para la mejor práctica de fecha
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale





class RegisterFragment : Fragment() {
    private var selectedBirthdate: String? = null

    private fun showDatePickerDialog(dateEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)



        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                // Formato que se guarda en la base de datos (y que usamos para el cálculo)
                val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedBirthdate = dbFormat.format(selectedCalendar.time)


                // Formato que se muestra al usuario
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateEditText.setText(displayFormat.format(selectedCalendar.time))
            },
            year,
            month,
            day
        )
        // Opcional: Impedir seleccionar fechas futuras para la fecha de nacimiento
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)


        val etNombre = view.findViewById<EditText>(R.id.etNombreRegistro)
        val etBirthdate = view.findViewById<EditText>(R.id.etEdadRegistro) // Mantendremos este ID, pero su propósito cambia
        val etUsername = view.findViewById<EditText>(R.id.etUsernameRegistro)
        val etPass = view.findViewById<EditText>(R.id.etPassRegistro)
        val btnRegistrar = view.findViewById<Button>(R.id.btnConfirmarRegistro)
        val tvVolver = view.findViewById<TextView>(R.id.tvVolverLogin)


        tvVolver.setOnClickListener {

            findNavController().navigateUp()
        }

        etBirthdate.setText("Seleccionar Fecha")
        etBirthdate.isFocusable = false
        etBirthdate.setOnClickListener {
            showDatePickerDialog(etBirthdate)
        }

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val pass = etPass.text.toString().trim()

            // Paso 3: validar fecha escrita manualmente
            val birthdateText = etBirthdate.text.toString().trim()
            val formatterDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatterDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            var birthdate: String? = null
            try {
                val parsedDate = formatterDisplay.parse(birthdateText)
                birthdate = formatterDb.format(parsedDate!!)
            } catch (e: Exception) {
                Toast.makeText(context, "Formato inválido. Usa DD/MM/AAAA.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            if (nombre.isEmpty() || username.isEmpty() || pass.isEmpty() || birthdate.isNullOrEmpty()) {
                Toast.makeText(context, "Por favor completa todos los datos, incluyendo la fecha de nacimiento", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(requireContext())
                    val existe = db.appDao().checkUserExists(username)

                    if (existe != null) {
                        Toast.makeText(context, "El nombre de usuario '$username' ya está registrado. Elige otro.", Toast.LENGTH_LONG).show()
                    } else {
                        val nuevoUsuario = UserEntity(
                            nombre = nombre,
                            birthdate = birthdate, // ahora siempre viene validada
                            username = username,
                            password = pass
                        )
                        db.appDao().registerUser(nuevoUsuario)
                        Toast.makeText(context, "¡Cuenta creada! Inicia sesión con tu usuario.", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        return view
    }
}


