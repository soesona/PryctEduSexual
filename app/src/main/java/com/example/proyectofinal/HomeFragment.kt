package com.example.proyectofinal

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinal.databinding.FragmentHomeBinding


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)



        binding.cardMethods.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_methods)
        }

        // Clic en la tarjeta de ETS
        binding.cardEts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_ets)
        }

        // Clic en la tarjeta de Mitos vs Realidad
        binding.cardMyths.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_myths)
        }


        actualizarPuntaje()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) {
            actualizarPuntaje()
        }
    }

    private fun actualizarPuntaje() {

        val sessionPrefs = requireActivity().getSharedPreferences("SesionApp", Context.MODE_PRIVATE)
        val userId = sessionPrefs.getInt("USER_ID", -1)

        if (userId != -1) {
            val prefs = requireActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE)


            val claveUsuario = "puntaje_global_$userId"

            val puntajeTotal = prefs.getInt(claveUsuario, 0)
            binding.tvScoreGlobal.text = "$puntajeTotal XP"
        } else {
            binding.tvScoreGlobal.text = "0 XP"
        }
    }
}