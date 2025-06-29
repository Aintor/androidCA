package iss.nus.edu.sg.androidca

import android.os.Bundle
import androidx.fragment.app.Fragment
import iss.nus.edu.sg.androidca.databinding.FragmentFetchBinding

class PlayFragment: Fragment() {
    private var _binding: FragmentFetchBinding ?= null
    private val binding get() = _binding!!
    companion object {
        fun newInstance(selectedData: Map<Int, String>): PlayFragment {
            val fragment = PlayFragment()
            val bundle = Bundle().apply {
                putSerializable("selectedData", HashMap(selectedData))
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}