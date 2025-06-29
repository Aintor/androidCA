package iss.nus.edu.sg.androidca

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import iss.nus.edu.sg.androidca.databinding.FetchViewBinding

class FetchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null): FrameLayout(context, attrs) {
    private val binding = FetchViewBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var imageCard: MaterialCardView
    private lateinit var imageView: ImageView
    private lateinit var checkHint: ImageView
    init {
        imageCard = binding.imageCard
        imageView = binding.imageView
        checkHint = binding.checkHint
        imageCard.setOnClickListener {
            //to do
        }
    }
}