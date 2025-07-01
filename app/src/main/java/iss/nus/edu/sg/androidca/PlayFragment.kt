package iss.nus.edu.sg.androidca

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import iss.nus.edu.sg.androidca.databinding.FragmentPlayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer

class PlayFragment: Fragment() {
    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!
    private var isPaid: Boolean = false
    private val cardViews = mutableListOf<CardView>()
    private var cardCount = 0
    private lateinit var selectedData: Map<Int, String>
    private lateinit var countdown: LinearLayout
    private lateinit var leftBar: TextView
    private lateinit var centerBar: TextView
    private lateinit var rightBar: TextView
    private lateinit var playHint: TextView
    private lateinit var stopWatch: TextView
    private lateinit var playGrid: GridLayout
    private lateinit var adCardView: MaterialCardView
    private lateinit var adImage: ImageView
    private lateinit var winAnimation: LottieAnimationView
    private lateinit var soundPool: SoundPool
    private lateinit var playMusicPlayer: MediaPlayer
    private lateinit var upbeatMusicPlayer: MediaPlayer
    private val ads = mutableListOf<Bitmap>()
    private var secondsElapsed = 0
    private var countdownSoundId = 0
    private var flipSoundId = 0
    private var correctSoundId = 0
    private var incorrectSoundId = 0
    private var gameoverSoundId = 0
    private var hintAnimator: Animator? = null
    private var stopwatchAnimator: Animator? = null
    private var timer: Timer? = null
    private var isFlippable = false
    private var flippedCards = mutableListOf<CardView>()

    companion object {
        fun newInstance(selectedData: Map<Int, String>, isPaid: Boolean): PlayFragment {
            val fragment = PlayFragment()
            val bundle = Bundle().apply {
                putSerializable("selectedData", HashMap(selectedData))
                putBoolean("isPaid", isPaid)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            selectedData = it.getSerializable("selectedData") as? HashMap<Int, String> ?: emptyMap()
            isPaid = it.getBoolean("isPaid")
        }
        initUI()
        initCards()
        initMusic()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAd()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timer?.cancel()
        soundPool.release()
        playMusicPlayer.release()
        upbeatMusicPlayer.release()
        hintAnimator?.cancel()
        stopwatchAnimator?.cancel()
    }

    fun initUI() {
        countdown = binding.countdown
        leftBar = binding.leftBar
        centerBar = binding.centerBar
        rightBar = binding.rightBar
        playHint = binding.playHint
        stopWatch = binding.stopwatch
        playGrid = binding.playGrid
        adCardView = binding.ad
        adImage = binding.adImage
        winAnimation = binding.winAnimation
    }

    fun initMusic() {
        soundPool = SoundPool.Builder().setMaxStreams(3).build()
        countdownSoundId = soundPool.load(requireContext(), R.raw.countdown, 1)
        flipSoundId = soundPool.load(requireContext(), R.raw.flip, 1)
        correctSoundId = soundPool.load(requireContext(), R.raw.correct, 1)
        incorrectSoundId = soundPool.load(requireContext(), R.raw.incorrect, 1)
        gameoverSoundId = soundPool.load(requireContext(), R.raw.gameover, 1)
        playMusicPlayer = MediaPlayer.create(requireContext(), R.raw.play)
        playMusicPlayer.isLooping = true
        upbeatMusicPlayer = MediaPlayer.create(requireContext(), R.raw.upbeat)
        upbeatMusicPlayer.isLooping = true
    }

    fun fadeInMediaPlayer(
        player: MediaPlayer?,
        duration: Long = 500L,
        onComplete: (() -> Unit)? = null
    ) {
        player?.setVolume(0f, 0f)
        player?.start()
        val steps = 20
        val delay = duration / steps
        for (i in 0..steps) {
            Handler(Looper.getMainLooper()).postDelayed({
                val volume = i / steps.toFloat()
                player?.setVolume(volume, volume)
                if (i == steps && onComplete != null) onComplete()
            }, i * delay)
        }
    }

    fun fadeOutMediaPlayer(
        player: MediaPlayer?,
        duration: Long = 500L,
        onComplete: (() -> Unit)? = null
    ) {
        val steps = 20
        val delay = duration / steps
        for (i in 0..steps) {
            Handler(Looper.getMainLooper()).postDelayed({
                val volume = (steps - i) / steps.toFloat()
                player?.setVolume(volume, volume)
                if (i == steps && onComplete != null) onComplete()
            }, i * delay)
        }
    }

    fun setAd() {
        if (isPaid) {
            adCardView.visibility = View.INVISIBLE
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val url = URL("${getString(R.string.base_url)}/Ad")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 10000
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Accept", "application/json")

                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val result = JSONArray(response)

                    for (i in 0 until result.length()) {
                        val base64 = result.getString(i)
                        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        ads.add(bitmap)
                    }

                    withContext(Dispatchers.Main) {
                        if (ads.isNotEmpty()) {
                            adImage.setImageBitmap(ads[0])
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun isFlippableNow(): Boolean = isFlippable

    fun initCards() {
        val cacheDir = requireContext().cacheDir
        playGrid.post {
            val cellWidth = playGrid.width / playGrid.columnCount
            val cellHeight = playGrid.height / playGrid.rowCount
            selectedData.forEach { (index, alt) ->
                val fileName = "$index.jpg"
                val file = File(cacheDir, fileName)
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                repeat(2) {
                    val cardview = CardView(requireContext())
                    cardview.layoutParams = GridLayout.LayoutParams().apply {
                        width = cellWidth
                        height = cellHeight
                    }
                    cardview.flipToFront()
                    cardview.setCard(index, bitmap, alt)
                    cardview.checkisFlippable = ::isFlippableNow
                    cardview.onFlipped = ::onFlipped
                    cardViews.add(cardview)
                }
            }
            cardViews.shuffle()
            cardViews.forEach { playGrid.addView(it) }
        }
    }

    fun start() {
        setCountdownBar()
        soundPool.play(countdownSoundId, 1f, 1f, 1, 0, 1f)
        view?.postDelayed({
            for (cardView in cardViews) {
                cardView.flipToBack()
            }
            isFlippable = true
            startStopwatch()
        }, 3000)
        view?.postDelayed({
            playHint.text = getString(R.string.play_hint, cardCount)
            stopWatch.text = getString(R.string.stop_watch, secondsElapsed)
            fadeInMediaPlayer(playMusicPlayer)
        }, 3500)
    }

    fun setCountdownBar() {
        leftBar.text = "3"
        centerBar.text = "2"
        rightBar.text = "1"

        view?.postDelayed({
            leftBar.visibility = View.VISIBLE
        }, 0)

        view?.postDelayed({
            leftBar.visibility = View.INVISIBLE
            centerBar.visibility = View.VISIBLE
        }, 1000)

        view?.postDelayed({
            centerBar.visibility = View.INVISIBLE
            rightBar.visibility = View.VISIBLE
        }, 2000)

        view?.postDelayed({
            rightBar.visibility = View.INVISIBLE
            centerBar.setTextColor(requireContext().getColor(R.color.green))
            centerBar.text = getString(R.string.game_start)
            centerBar.visibility = View.VISIBLE
        }, 3000)
        view?.postDelayed({
            countdown.visibility = View.GONE
        }, 3500)
    }

    fun startStopwatch() {
        secondsElapsed = 0
        timer = Timer()
        fun scheduleNextTick() {
            timer?.schedule(object : java.util.TimerTask() {
                override fun run() {
                    requireActivity().runOnUiThread {
                        secondsElapsed += 1
                        stopWatch.text = getString(R.string.stop_watch, secondsElapsed)
                        scheduleNextTick()
                        if (!isPaid && (secondsElapsed + 3) % 30 == 0) {
                            Log.d("PlayFragment", ads.size.toString())
                            val adIndex = ((secondsElapsed + 3) / 30) % ads.size
                            adImage.setImageBitmap(ads[adIndex])
                        }
                    }
                }
            }, 1000)
        }
        scheduleNextTick()
    }

    fun onFlipped(cardView: CardView) {
        soundPool.play(flipSoundId, 1f, 1f, 1, 0, 1f)
        flippedCards.add(cardView)
        if (flippedCards.size == 2) {
            view?.postDelayed({
                isFlippable = false
                if (flippedCards[0].Index == flippedCards[1].Index) {
                    soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
                    cardCount += 1
                    for (cardView in flippedCards) { cardView.isFlipped = true }
                    flippedCards.clear()
                    isFlippable = true
                    updateStatus()
                } else {
                    soundPool.play(incorrectSoundId, 1f, 1f, 1, 0, 1f)
                    view?.postDelayed({
                        for (card in flippedCards) {
                            card.flipToBack()
                        }
                        flippedCards.clear()
                        isFlippable = true
                    }, 400)
                }
            }, 100)
        }
    }
    fun updateStatus() {
        playHint.text = getString(R.string.play_hint, cardCount)
        if (cardCount == 4) {
            changeUpbeat()
        }
        if (cardCount == selectedData.size) {
            timer?.cancel()
            hintAnimator?.cancel()
            stopwatchAnimator?.cancel()
            upbeatMusicPlayer.stop()
            winAnimation.visibility = View.VISIBLE
            winAnimation.playAnimation()
            soundPool.play(gameoverSoundId, 1f, 1f, 1, 0, 1f)
            winAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(p0: Animator) {
                    (activity as? MainActivity)?.isPlaySuccessful(secondsElapsed)
                }
            })
        }
    }

    fun changeUpbeat() {
        fadeOutMediaPlayer(playMusicPlayer)
        fadeInMediaPlayer(upbeatMusicPlayer)
        playHint.setTextColor(requireContext().getColor(R.color.red))
        hintAnimator = createUpbeatAnimation(playHint)
        hintAnimator?.start()
        stopWatch.setTextColor(requireContext().getColor(R.color.red))
        stopwatchAnimator = createUpbeatAnimation(stopWatch)
        stopwatchAnimator?.start()
    }

    fun createUpbeatAnimation(view: View): Animator {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 1.1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 1.1f)

        val duration = 500L
        scaleX.duration = duration
        scaleY.duration = duration

        scaleX.repeatMode = ObjectAnimator.REVERSE
        scaleY.repeatMode = ObjectAnimator.REVERSE

        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleY.repeatCount = ObjectAnimator.INFINITE

        val breathingAnimator = AnimatorSet()
        breathingAnimator.play(scaleX).with(scaleY)

        return breathingAnimator
    }
}