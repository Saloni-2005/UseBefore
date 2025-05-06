package com.example.usebefore.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.usebefore.R
import com.example.usebefore.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // You can dynamically set content here if needed
        // binding.tvInfoContent.text = getString(R.string.info_content)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}