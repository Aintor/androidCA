package iss.nus.edu.sg.androidca

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import iss.nus.edu.sg.androidca.databinding.FragmentLeaderboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LeaderboardFragment : Fragment() {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var leaderboard: LeaderboardView
    private lateinit var backButton: MaterialButton
    private lateinit var username: String
    private var userRank: Int = 0
    private var secondsElapsed: Int = 0
    private val leaderboardEntries = mutableListOf<LeaderboardEntry>()

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
        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            username = it.getString("username") ?: ""
            secondsElapsed = it.getInt("secondsElapsed")
        }
        initUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchLeaderboard(username, secondsElapsed)
        backButton.setOnClickListener {
            (activity as? MainActivity)?.backToFetch()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initUI() {
        leaderboard = binding.leaderboard
        backButton = binding.backButton
    }
    fun setData() {
        leaderboard.setData(leaderboardEntries, if (userRank > 0 || userRank < 6) userRank else null)
    }

    fun fetchLeaderboard(username: String, bestTime: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${getString(R.string.base_url)}/Leaderboard")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 10000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val json = JSONObject()
                json.put("username", username)
                json.put("besttime", bestTime)

                conn.outputStream.use {
                    it.write(json.toString().toByteArray())
                }

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val result = JSONObject(response)

                val top5List = result.getJSONArray("top5")
                userRank = result.getInt("yourRank")

                for (i in 0 until top5List.length()) {
                    val item = top5List.getJSONObject(i)
                    leaderboardEntries.add(
                        LeaderboardEntry(
                            rank = i + 1,
                            username = item.getString("username"),
                            time = item.getInt("bestTime")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}