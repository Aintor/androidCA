package iss.nus.edu.sg.androidca

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import iss.nus.edu.sg.androidca.databinding.ActivityMainBinding
import iss.nus.edu.sg.androidca.databinding.FragmentLeaderboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragment_container: FragmentContainerView
    private lateinit var loading_animation: FrameLayout
    private lateinit var username: String
    private var isPaid = false
    private val loadingDelay = 2000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initUI()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(fragment_container.id, LoginFragment())
                .commit()
        }
    }
    fun initUI() {
        fragment_container = binding.fragmentContainer
        loading_animation = binding.loadingAnimationLayout
    }

    fun displayLoadingAnimation() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                loading_animation.visibility = View.VISIBLE
            }
            delay(loadingDelay)
            withContext(Dispatchers.Main) {
                loading_animation.visibility = View.GONE
            }
        }
    }

    fun isLoginSuccessful(username: String, isPaid: Boolean) {
        this.username = username
        this.isPaid = isPaid
        fragment_container.visibility = View.INVISIBLE
        displayLoadingAnimation()
        supportFragmentManager.beginTransaction()
            .replace(fragment_container.id, FetchFragment())
            .commit()
        lifecycleScope.launch {
            delay(2000)
            withContext(Dispatchers.Main) {
               fragment_container.visibility = View.VISIBLE
            }
        }

    }

    fun isCheckSuccessful(selectedData: Map<Int, String>) {
        fragment_container.visibility = View.INVISIBLE
        displayLoadingAnimation()
        supportFragmentManager.beginTransaction()
            .replace(fragment_container.id, PlayFragment.newInstance(selectedData, isPaid), "PLAY")
            .commit()
        lifecycleScope.launch {
            delay(loadingDelay)
            withContext(Dispatchers.Main) {
                fragment_container.visibility = View.VISIBLE
                val playFragment = supportFragmentManager.findFragmentByTag("PLAY") as? PlayFragment
                playFragment?.start()
            }
        }
    }

    fun isPlaySuccessful(username: String, secondsElapsed: Int) {
        fragment_container.visibility = View.INVISIBLE
        displayLoadingAnimation()
        supportFragmentManager.beginTransaction()
            .replace(fragment_container.id, LeaderboardFragment.newInstance(username, secondsElapsed))
            .commit()
        lifecycleScope.launch {
            delay(loadingDelay)
            withContext(Dispatchers.Main) {
                fragment_container.visibility = View.VISIBLE
            }
        }
    }
}