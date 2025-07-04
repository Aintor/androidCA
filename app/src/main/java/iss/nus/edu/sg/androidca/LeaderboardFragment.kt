package iss.nus.edu.sg.androidca

import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import iss.nus.edu.sg.androidca.databinding.FragmentLeaderboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
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
    private lateinit var soundPool: SoundPool
    private var leaderboardSoundId = 0

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
        initSound()
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
        soundPool.release()
    }

    fun initUI() {
        leaderboard = binding.leaderboard
        backButton = binding.backButton
    }

    fun initSound() {
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        leaderboardSoundId = soundPool.load(requireContext(), R.raw.leaderboard, 1)
    }
    fun setData() {
        soundPool.play(leaderboardSoundId, 1f, 1f, 0, 0, 1f)
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
                MotionToast.createToast(requireActivity(),
                    "Error",
                    getString(R.string.error_message),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(requireActivity(),R.font.normal))
            }
        }
    }
}