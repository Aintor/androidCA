package iss.nus.edu.sg.androidca

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import iss.nus.edu.sg.androidca.databinding.FragmentPlayBinding
import java.io.File
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
    private lateinit var adImage: ImageView
    private lateinit var winAnimation: LottieAnimationView
    private lateinit var soundPool: SoundPool
    private lateinit var playMusicPlayer: MediaPlayer
    private lateinit var upbeatMusicPlayer: MediaPlayer
    private var secondsElapsed = 0
    private var countdownSoundId = 0
    private var flipSoundId = 0
    private var correctSoundId = 0
    private var incorrectSoundId = 0
    private var gameoverSoundId = 0
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
    }

    fun initUI() {
        countdown = binding.countdown
        leftBar = binding.leftBar
        centerBar = binding.centerBar
        rightBar = binding.rightBar
        playHint = binding.playHint
        stopWatch = binding.stopwatch
        playGrid = binding.playGrid
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
            adImage.visibility = View.INVISIBLE
        } else {
            //to fetch ad image
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
            upbeatMusicPlayer.stop()
            winAnimation.visibility = View.VISIBLE
            winAnimation.playAnimation()
            soundPool.play(gameoverSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun changeUpbeat() {
        fadeOutMediaPlayer(playMusicPlayer)
        fadeInMediaPlayer(upbeatMusicPlayer)
        playHint.setTextColor(requireContext().getColor(R.color.red))
        stopWatch.setTextColor(requireContext().getColor(R.color.red))
    }
}