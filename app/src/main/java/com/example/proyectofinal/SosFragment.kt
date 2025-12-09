package com.example.proyectofinal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.proyectofinal.databinding.FragmentSosBinding

class SosFragment : Fragment(R.layout.fragment_sos) {

    private var contactoGuardado: String? = null

    private var userIdActual = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSosBinding.bind(view)


        val sessionPrefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        userIdActual = sessionPrefs.getInt("USER_ID", -1)

        val prefs = requireActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE)


        if (userIdActual != -1) {
            val claveContacto = "sos_contacto_$userIdActual"
            contactoGuardado = prefs.getString(claveContacto, null)
        }

        actualizarTextoContacto(binding)


        binding.btnEmergency.setOnClickListener {
            abrirMarcador("911")
        }


        binding.btnTrusted.setOnClickListener {
            if (contactoGuardado.isNullOrEmpty()) {
                mostrarDialogoConfigurar(binding, prefs)
            } else {
                abrirMarcador(contactoGuardado!!)
            }
        }


        binding.btnEditTrusted.setOnClickListener {
            mostrarDialogoConfigurar(binding, prefs)
        }


        binding.btnHelplineGender.setOnClickListener {
            abrirMarcador("114")
        }


    }

    private fun abrirMarcador(numero: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$numero")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se puede abrir el teléfono", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoConfigurar(binding: FragmentSosBinding, prefs: android.content.SharedPreferences) {

        if (userIdActual == -1) {
            Toast.makeText(context, "Error: Inicia sesión para guardar contactos", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_PHONE
        input.hint = "Ej: 99991234"

        AlertDialog.Builder(requireContext())
            .setTitle("Contacto de Confianza")
            .setMessage("Escribe el número de la persona a quien llamarás en caso de emergencia:")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNumero = input.text.toString()
                if (nuevoNumero.isNotEmpty()) {


                    val claveContacto = "sos_contacto_$userIdActual"
                    prefs.edit().putString(claveContacto, nuevoNumero).apply()

                    contactoGuardado = nuevoNumero
                    actualizarTextoContacto(binding)
                    Toast.makeText(context, "Contacto privado guardado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarTextoContacto(binding: FragmentSosBinding) {
        if (!contactoGuardado.isNullOrEmpty()) {
            binding.tvTrustedNumber.text = "Llamar a: $contactoGuardado"
        } else {
            binding.tvTrustedNumber.text = "Toca para configurar número"
        }
    }
}