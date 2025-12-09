package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinal.databinding.FragmentEtsBinding


class EtsFragment : Fragment(R.layout.fragment_ets) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentEtsBinding.bind(view)

        // Botón para lanzar el Quiz de ETS
        binding.btnLaunchQuizEts.setOnClickListener {
            // Preparamos el paquete con la categoría "ets"
            val bundle = Bundle()
            bundle.putString("categoria", "ets")

            try {

                findNavController().navigate(R.id.action_etsFragment_to_quizFragment, bundle)
            } catch (e: Exception) {

                Toast.makeText(context, "Falta conectar ETS con Quiz en nav_graph", Toast.LENGTH_LONG).show()
            }
        }
    }
}