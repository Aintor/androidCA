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
            delay(1500)
            withContext(Dispatchers.Main) {
                loading_animation.visibility = View.GONE
            }
        }
    }

    fun isLoginSuccessful(username: String, isPaid: Boolean) {
        this.username = username
        this.isPaid = isPaid
        displayLoadingAnimation()
        lifecycleScope.launch {
            delay(500)
            withContext(Dispatchers.Main) {
                supportFragmentManager.beginTransaction()
                    .replace(fragment_container.id, FetchFragment())
                    .commit()
            }
        }
    }

    fun isCheckSuccessful(selectedData: Map<Int, String>) {
        displayLoadingAnimation()
        lifecycleScope.launch {
            delay(500)
            withContext(Dispatchers.Main) {
                supportFragmentManager.beginTransaction()
                    .replace(fragment_container.id, PlayFragment.newInstance(selectedData))
                    .commit()
            }
        }
    }
}