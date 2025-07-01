package iss.nus.edu.sg.androidca

import android.animation.ObjectAnimator
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import iss.nus.edu.sg.androidca.databinding.FragmentFetchBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class FetchFragment: Fragment() {
    private var _binding: FragmentFetchBinding ?= null
    private val binding get() = _binding!!
    private var fetchJob: Job? = null
    private val fetchViews = mutableListOf<FetchView>()
    private val alts = mutableListOf<String>()
    private val selectedIndexes = mutableListOf<Int>()
    private var isAddable = true
    private var isRemovable = false
    private var isFetched = false
    private lateinit var url: TextInputEditText
    private lateinit var fetchButton: MaterialButton
    private lateinit var fetch_status: TextView
    private lateinit var loading_bar: LottieAnimationView
    private lateinit var fetch_grid: GridLayout
    private lateinit var fetch_card: MaterialCardView
    private lateinit var check_button: ImageButton
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        _binding = FragmentFetchBinding.inflate(inflater, container, false)
        initUI()
        fetch_grid.post {
            val cellWidth = fetch_grid.width / 4
            val cellHeight = fetch_grid.height / 5
            repeat(20) { index ->
                val fetchView = FetchView(requireContext())
                fetchView.layoutParams = GridLayout.LayoutParams().apply {
                    width = cellWidth
                    height = cellHeight
                }
                fetchView.checkIsFetched = ::isFetchedNow
                fetchView.checkIsAddable = ::isAddableNow
                fetchView.checkIsRemovable = ::isRemovableNow
                fetchView.onSelected = ::onSelected
                fetchView.onDeselected = ::onDeselected
                fetchView.position = index
                fetchViews.add(fetchView)
                fetch_grid.addView(fetchView)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchButton.setOnClickListener {
            isFetched = false
            fetchJob?.cancel()
            for (fetchView in fetchViews) {
                fetchView.resetFetchView()
            }
            alts.clear()
            selectedIndexes.clear()
            loading_bar.visibility = View.INVISIBLE
            resetLottieAnimation(loading_bar)
            val cacheDir = requireContext().cacheDir
            cacheDir.listFiles()?.forEach { it.delete() }
            val urlText = url.text.toString()
            fetchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main) {
                        fetch_status.text = getString(R.string.resolve_url)
                    }
                    val document = Jsoup.connect(urlText).get()
                    val images = document.select("img")
                    if (images.isEmpty()) {
                        throw Exception("Can not resolve the url")
                    }
                    var count = 0
                    withContext(Dispatchers.Main) {
                        fetch_status.text = getString(R.string.fetch_status, count)
                        loading_bar.visibility = View.VISIBLE
                        updateLottieProgress(loading_bar, count*5f)
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
                                val connection = Jsoup.connect(src)
                                    .ignoreContentType(true)
                                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0 Safari/537.36")
                                val urlStream = connection.execute().bodyStream()
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
                                    updateLottieProgress(loading_bar, count*5f)
                                    alts.add(alt)
                                }
                                count++
                            } catch (e: Exception) {
                                Log.e("FetchFragment", "Image fetch process failed: ${e.message}")
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        updateLottieProgress(loading_bar, count*100f, false)
                        checkState()
                        isFetched = true
                    }
                } catch (e: Exception) {
                    Log.e("FetchFragment", "Image fetch start failed: ${e.message}")
                    for (fetchView in fetchViews) {
                        fetchView.resetFetchView()
                    }
                    withContext(Dispatchers.Main) {
                        fetch_status.text = ""
                        loading_bar.visibility = View.INVISIBLE
                        resetLottieAnimation(loading_bar)
                        MotionToast.createToast(requireActivity(),
                            "Error",
                            getString(R.string.error_fetch),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(requireActivity(),R.font.normal))
                    }
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
        loading_bar.cancelAnimation()
    }

    fun initUI() {
        url = binding.urlInput
        fetchButton = binding.fetchButton
        loading_bar = binding.loadingBar
        fetch_status = binding.fetchStatus
        fetch_grid = binding.fetchGrid
        fetch_card = binding.fetchCard
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
    fun updateLottieProgress(lottieView: LottieAnimationView, percentage: Float, animated: Boolean = true) {
        val lottieProgress = (percentage / 100.0f).coerceIn(0.0f, 1.0f)
        if (animated) {
            val animator = ObjectAnimator.ofFloat(lottieView, "progress", lottieView.progress, lottieProgress)
            animator.duration = 300
            animator.start()
        } else {
            lottieView.progress = lottieProgress
        }
    }

    fun resetLottieAnimation(lottieView: LottieAnimationView) {
        lottieView.progress = 0f
    }
}