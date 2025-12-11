package com.example.proyectofinal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import android.widget.TextView



class LoginFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val etUserLogin = view.findViewById<EditText>(R.id.etUserLogin)
        val etPass = view.findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)











        tvForgotPassword.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_loginFragment_to_changePasswordFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Error de navegación: verifica tu nav_graph", Toast.LENGTH_SHORT).show()
            }

        }

        tvGoToRegister.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_login_to_register)
            } catch (e: Exception) {
                Toast.makeText(context, "Error de navegación: verifica tu nav_graph", Toast.LENGTH_SHORT).show()
            }
        }


        btnLogin.setOnClickListener {
            val username = etUserLogin.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (username.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "Por favor ingresa el usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(requireContext())


                    val user = db.appDao().login(username, pass)

                    if (user != null) {

                        val prefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
                        prefs.edit().putInt("USER_ID", user.id).apply()


                        Toast.makeText(context, "¡Bienvenido, ${user.nombre}!", Toast.LENGTH_SHORT).show()


                        findNavController().navigate(R.id.action_login_to_home)
                    } else {

                        Toast.makeText(context, "Usuario no encontrado o contraseña incorrecta", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al conectar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}