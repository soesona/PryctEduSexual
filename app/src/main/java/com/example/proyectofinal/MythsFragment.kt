package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectofinal.databinding.FragmentMythsBinding

class MythsFragment : Fragment(R.layout.fragment_myths) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMythsBinding.bind(view)


        binding.btnLaunchQuizMyths.setOnClickListener {


            val bundle = Bundle()
            bundle.putString("categoria", "mitos")

            try {

                findNavController().navigate(R.id.action_mythsFragment_to_quizFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: Falta conectar Mitos con Quiz en nav_graph", Toast.LENGTH_LONG).show()
            }
        }
    }
}