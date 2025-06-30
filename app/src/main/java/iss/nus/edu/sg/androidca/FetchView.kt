package iss.nus.edu.sg.androidca

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import iss.nus.edu.sg.androidca.databinding.FetchViewBinding

class FetchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0): FrameLayout(context, attrs, defStyleAttr) {
    private val binding = FetchViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val imageCard: MaterialCardView = binding.imageCard
    private val rootLayout: ConstraintLayout = binding.rootLayout
    private val imageView: ImageView = binding.imageView
    private val checkHint: ImageView = binding.checkHint
    private val loadingAnimation: LottieAnimationView = binding.loadingCard
    var checkIsFetched: (() -> Boolean)? = null
    var checkIsAddable: (() -> Boolean)? = null
    var checkIsRemovable: (() -> Boolean)? = null
    var position: Int? = null
    var onSelected: ((Int) -> Unit)? = null
    var onDeselected: ((Int) -> Unit)? = null
    init {
        imageCard.setOnClickListener {
            if (checkIsFetched?.invoke() == true) {
                if (checkHint.visibility == GONE && checkIsAddable?.invoke() == true) {
                    position?.let { onSelected?.invoke(it) }
                    imageCard.strokeColor = ContextCompat.getColor(context, R.color.green)
                    checkHint.visibility = VISIBLE
                } else if (checkHint.visibility == VISIBLE && checkIsRemovable?.invoke() == true) {
                    position?.let { onDeselected?.invoke(it) }
                    imageCard.strokeColor = ContextCompat.getColor(context, android.R.color.transparent)
                    checkHint.visibility = GONE
                }
            }
        }
    }

    fun setVisible() {
        rootLayout.visibility = VISIBLE
    }

    fun setImage(bitmap: Bitmap, alt: String) {
        loadingAnimation.visibility = GONE
        imageView.setImageBitmap(bitmap)
        imageView.contentDescription = alt
    }

    fun resetFetchView() {
        rootLayout.visibility = INVISIBLE
        loadingAnimation.visibility = VISIBLE
        imageCard.strokeColor = ContextCompat.getColor(context, android.R.color.transparent)
        checkHint.visibility = GONE
    }
}