package iss.nus.edu.sg.androidca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import iss.nus.edu.sg.androidca.databinding.FragmentFetchBinding

class FetchFragment: Fragment() {
    private var _binding: FragmentFetchBinding ?= null
    private val binding get() = _binding!!
    private lateinit var url: TextInputEditText
    private lateinit var fetchButton: MaterialButton
    private lateinit var fetch_status: TextView
    private lateinit var fetch_grid: GridLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        _binding = FragmentFetchBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchButton.setOnClickListener {
            //to implement
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initUI() {
        url = binding.urlInput
        fetchButton = binding.fetchButton
        fetch_status = binding.fetchStatus
        fetch_grid = binding.fetchGrid
    }
}