package iss.nus.edu.sg.androidca

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import iss.nus.edu.sg.androidca.databinding.CardViewBinding
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd

class CardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr){
    private val binding = CardViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val cardFront = binding.cardFront
    private val cardBack = binding.cardBack
    private val cardLayout = binding.cardLayout
    private val animationDuration = 150L
    private var isAnimating: Boolean = false
    var onFlipped: ((CardView) -> Unit)? = null
    var checkisFlippable: (() -> Boolean)? = null
    var isFlipped: Boolean = false
    var Index: Int? = null
    init {
        cardLayout.setOnClickListener {
            if (!isFlipped && checkisFlippable?.invoke() == true && cardBack.visibility == VISIBLE && !isAnimating) {
                onFlipped?.invoke(this)
                flipToFront()
            }
        }
    }

    fun setCard(index: Int,bitmap: Bitmap, alt: String){
        this.Index = index
        cardFront.setImageBitmap(bitmap)
        cardFront.contentDescription = alt
    }

fun flipToFront() {
    if (isAnimating) return
    isAnimating = true
    isFlipped = true

    val flipOut = ObjectAnimator.ofFloat(this, ROTATION_Y, 0f, 90f).apply {
        duration = animationDuration
        interpolator = AccelerateInterpolator()
    }

    val flipIn = ObjectAnimator.ofFloat(this, ROTATION_Y, -90f, 0f).apply {
        duration = animationDuration
        interpolator = DecelerateInterpolator()
    }

    flipOut.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            cardBack.visibility = INVISIBLE
            cardFront.visibility = VISIBLE
            rotationY = -90f
            flipIn.start()
        }
    })

    flipIn.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            isAnimating = false
        }
    })

    flipOut.start()
}

    fun flipToBack() {
        if (isAnimating) return
        isAnimating = true
        isFlipped = false

        val flipOut = ObjectAnimator.ofFloat(this, ROTATION_Y, 0f, 90f).apply {
            duration = animationDuration
            interpolator = AccelerateInterpolator()
        }

        val flipIn = ObjectAnimator.ofFloat(this, ROTATION_Y, -90f, 0f).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
        }

        flipOut.doOnEnd {
            cardFront.visibility = INVISIBLE
            cardBack.visibility = VISIBLE
            rotationY = -90f
            flipIn.start()
        }

        flipIn.doOnEnd {
            isAnimating = false
        }

        flipOut.start()
    }
}