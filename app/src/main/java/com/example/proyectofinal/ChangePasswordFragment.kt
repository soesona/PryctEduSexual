package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    private lateinit var etUserChange: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnVolver: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserChange = view.findViewById(R.id.etUserChange)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnVolver = view.findViewById(R.id.btnVolver)

        btnChangePassword.setOnClickListener {
            val username = etUserChange.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()

            if (username.isBlank()) {
                etUserChange.error = "El usuario no puede estar vacío"
                return@setOnClickListener
            }
            if (newPass.isBlank()) {
                etNewPassword.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                val usuario = db.appDao().getUserByUsername(username)

                if (usuario == null) {
                    Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val actualizado = usuario.copy(password = newPass)
                db.appDao().updateProfile(actualizado)

                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_LONG).show()
            }
        }

        btnVolver.setOnClickListener {
            findNavController().navigateUp()

        }
    }
}