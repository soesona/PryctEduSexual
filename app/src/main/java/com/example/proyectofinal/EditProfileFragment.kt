package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditarPerfilFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var etNombre: EditText
    private lateinit var etBirthdate: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvUsername: TextView
    private lateinit var btnGuardar: Button

    private var usuarioActual: UserEntity? = null

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


                if (selectedCalendar.after(Calendar.getInstance())) {
                    Toast.makeText(context, "La fecha no puede ser futura", Toast.LENGTH_LONG)
                        .show()
                    return@DatePickerDialog
                }


                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateEditText.setText(displayFormat.format(selectedCalendar.time))
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNombre = view.findViewById(R.id.etNombre)
        etBirthdate = view.findViewById(R.id.etBirthdate)
        etPassword = view.findViewById(R.id.etPassword)
        tvUsername = view.findViewById(R.id.tvUsername)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        etBirthdate.setOnClickListener {
            showDatePickerDialog(etBirthdate)
        }


        val prefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(context, "Error: no se encontró sesión activa", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val usuario = db.appDao().getUserById(userId)

            if (usuario == null) {
                Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            usuarioActual = usuario

            tvUsername.text = "${usuario.username}"
            etNombre.setText(usuario.nombre)
            etPassword.setText(usuario.password)

            try {
                val formatterDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatterDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsedDate = formatterDb.parse(usuario.birthdate)
                etBirthdate.setText(formatterDisplay.format(parsedDate!!))
            } catch (e: Exception) {
                etBirthdate.setText(usuario.birthdate)
            }
        }


        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val birthdateText = etBirthdate.text.toString().trim()

            val formatterDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatterDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val birthdate: String = try {
                val parsedDate = formatterDisplay.parse(birthdateText)!!
                formatterDb.format(parsedDate)
            } catch (e: Exception) {
                etBirthdate.error = "Formato inválido. Usa DD/MM/AAAA."
                return@setOnClickListener
            }

            if (nombre.isBlank()) {
                etNombre.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }
            if (pass.isBlank()) {
                etPassword.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            }

            val usuario = usuarioActual ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val actualizado = usuario.copy(
                        nombre = nombre,
                        birthdate = birthdate,
                        password = pass
                    )
                    val db = AppDatabase.getDatabase(requireContext())
                    db.appDao().updateProfile(actualizado)
                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    Toast.makeText(context, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

