package iss.nus.edu.sg.androidca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import iss.nus.edu.sg.androidca.databinding.FragmentFetchBinding

class FetchFragment: Fragment() {
    private var _binding: FragmentFetchBinding ?= null
    private val binding get() = _binding!!
    private var fetchJob: Job? = null
    private var isCheckable = false
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
            fetchJob?.cancel()
            val urlText = url.text.toString()
            fetchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val document = Jsoup.connect(urlText).get()
                    val images = document.select("img")
                    var count = 0
                    val cacheDir = requireContext().cacheDir
                    for (img in images) {
                        val src = img.absUrl("src")
                        val alt = img.attr("alt").ifBlank { "image_$count" }
                        if (!src.endsWith(".svg")) {
                            try {
                                val urlStream = URL(src).openStream()
                                val safeAlt = alt.replace(Regex("[^a-zA-Z0-9-_]"), "_")
                                val fileName = "$safeAlt.jpg"
                                val file = File(cacheDir, fileName)
                                val output = FileOutputStream(file)
                                urlStream.use { input ->
                                    output.use { out ->
                                        input.copyTo(out)
                                    }
                                }
                                count++
                                withContext(Dispatchers.Main) {
                                    fetch_status.text = "Fetched $count images..."
                                }
                            } catch (e: Exception) {
                                // Skip faulty image
                            }
                        }
                        if (count >= 20) break
                    }
                    withContext(Dispatchers.Main) {
                        fetch_status.text = "Completed. Total: $count images."
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        fetch_status.text = "Error: ${e.message}"
                    }
                }
            }
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