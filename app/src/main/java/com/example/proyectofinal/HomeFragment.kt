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


        binding.cardEts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_ets)
        }


        binding.cardMyths.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_myths)
        }




    }

    }


