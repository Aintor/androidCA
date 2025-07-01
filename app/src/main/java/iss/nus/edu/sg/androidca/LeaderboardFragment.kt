package iss.nus.edu.sg.androidca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import iss.nus.edu.sg.androidca.databinding.FragmentLeaderboardBinding
import iss.nus.edu.sg.androidca.databinding.FragmentPlayBinding

class LeaderboardFragment : Fragment() {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(username: String, secondsElapsed: Int): LeaderboardFragment {
            val fragment = LeaderboardFragment()
            val bundle = Bundle().apply {
                putString("username", username)
                putInt("secondsElapsed", secondsElapsed)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        //to fetch username and time
//        arguments?.let {
//            @Suppress("UNCHECKED_CAST")
//            selectedData = it.getSerializable("selectedData") as? HashMap<Int, String> ?: emptyMap()
//            isPaid = it.getBoolean("isPaid")
//        }
        return binding.root
    }
}