package com.example.proyectofinal

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinal.databinding.FragmentMethodsBinding

class MethodsFragment : Fragment(R.layout.fragment_methods) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMethodsBinding.bind(view)


        binding.filterTodos.setOnClickListener {
            filtrar(binding, "todos")
            actualizarBotones(binding, binding.filterTodos)
        }
        binding.filterHormonal.setOnClickListener {
            filtrar(binding, "hormonal")
            actualizarBotones(binding, binding.filterHormonal)
        }
        binding.filterBarrera.setOnClickListener {
            filtrar(binding, "barrera")
            actualizarBotones(binding, binding.filterBarrera)
        }
        binding.filterPerm.setOnClickListener {
            filtrar(binding, "permanente")
            actualizarBotones(binding, binding.filterPerm)
        }


        binding.btnLaunchQuizMethods.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("categoria", "metodos")

            try {

                findNavController().navigate(R.id.action_methodsFragment_to_quizFragment, bundle)
            } catch (e: Exception) {

                Toast.makeText(context, "Error de navegación: Revisa el nav_graph", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun filtrar(binding: FragmentMethodsBinding, tipo: String) {

        binding.cardPildora.isVisible = (tipo == "todos" || tipo == "hormonal")
        binding.cardDiu.isVisible = (tipo == "todos" || tipo == "hormonal")
        binding.cardCondon.isVisible = (tipo == "todos" || tipo == "barrera")
        binding.cardVasec.isVisible = (tipo == "todos" || tipo == "permanente")
    }

    private fun actualizarBotones(binding: FragmentMethodsBinding, btnActivo: View) {

        val colorInactivo = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
        val colorActivo = ColorStateList.valueOf(Color.parseColor("#6200EE"))


        binding.filterTodos.backgroundTintList = colorInactivo
        binding.filterHormonal.backgroundTintList = colorInactivo
        binding.filterBarrera.backgroundTintList = colorInactivo
        binding.filterPerm.backgroundTintList = colorInactivo


        btnActivo.backgroundTintList = colorActivo
    }
}