package iss.nus.edu.sg.androidca

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
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
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class FetchFragment: Fragment() {
    private var _binding: FragmentFetchBinding ?= null
    private val binding get() = _binding!!
    private var fetchJob: Job? = null
    private val fetchViews = mutableListOf<FetchView>()
    private val alts = mutableSetOf<String>()
    private val selectedIndexes = mutableSetOf<Int>()
    private var isAddable = true
    private var isRemovable = false
    private var isFetched = false
    private lateinit var url: TextInputEditText
    private lateinit var fetchButton: MaterialButton
    private lateinit var fetch_status: TextView
    private lateinit var fetch_grid: GridLayout
    private lateinit var check_button: ImageButton
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
            isFetched = false
            fetchJob?.cancel()
            fetchViews.clear()
            alts.clear()
            selectedIndexes.clear()
            fetch_grid.removeAllViews()
            repeat(20) { index ->
                val fetchView = FetchView(requireContext())
                fetchView.checkIsFetched = ::isFetchedNow
                fetchView.checkIsAddable = ::isAddableNow
                fetchView.checkIsRemovable = ::isRemovableNow
                fetchView.onSelected = ::onSelected
                fetchView.onDeselected = ::onDeselected
                fetchView.position = index
                fetchViews.add(fetchView)
                fetch_grid.addView(fetchView)
            }
            val urlText = url.text.toString()
            fetchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val document = Jsoup.connect(urlText).get()
                    val images = document.select("img")
                    var count = 0
                    val cacheDir = requireContext().cacheDir
                    withContext(Dispatchers.Main) {
                        fetch_status.text = getString(R.string.fetch_status, count)
                        fetch_grid.visibility = View.VISIBLE
                    }
                    for (img in images) {
                        if (count >= 20) break
                        withContext(Dispatchers.Main) {
                            fetchViews[count].setVisible()
                            fetch_status.text = getString(R.string.fetch_status, count + 1)
                        }
                        val src = img.absUrl("src")
                        val alt = img.attr("alt").ifBlank { "image_$count" }
                        if (!src.endsWith(".svg")) {
                            try {
                                val urlStream = URL(src).openStream()
                                val fileName = "$count.jpg"
                                val file = File(cacheDir, fileName)
                                val output = FileOutputStream(file)
                                urlStream.use { input ->
                                    output.use { out ->
                                        input.copyTo(out)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                    fetchViews[count].setImage(bitmap, alt)
                                    alts.add(alt)
                                }
                                count++
                            } catch (e: Exception) {
                                Log.e("FetchFragment", "Image fetch process failed: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FetchFragment", "Image fetch start failed: ${e.message}")
                    fetchViews.clear()
                    fetch_grid.removeAllViews()
                    fetch_grid.visibility = View.INVISIBLE
                    withContext(Dispatchers.Main) {
                        MotionToast.createToast(requireActivity(),
                            "Error",
                            getString(R.string.error_fetch),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(requireActivity(),R.font.normal))
                    }
                }
                withContext(Dispatchers.Main) {
                    checkState()
                    isFetched = true
                }
            }
        }
        check_button.setOnClickListener {
            val selectedData = selectedIndexes.associateWith { index ->
                alts.elementAt(index)
            }
            (activity as? MainActivity)?.isCheckSuccessful(selectedData)
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
        check_button = binding.checkButton
    }

    fun isFetchedNow(): Boolean = isFetched
    fun isAddableNow(): Boolean = isAddable
    fun isRemovableNow(): Boolean = isRemovable

    fun onSelected(index: Int) {
        selectedIndexes.add(index)
        checkState()
    }
    fun onDeselected(index: Int) {
        selectedIndexes.remove(index)
        checkState()
    }
    fun checkState() {
        fetch_status.text = getString(R.string.pick_status, selectedIndexes.size)
        if (selectedIndexes.size == 6) {
            check_button.visibility = View.VISIBLE
        } else {
            check_button.visibility = View.GONE
        }
        isAddable = selectedIndexes.size < 6
        isRemovable = selectedIndexes.isNotEmpty()
    }
}