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
    private val imageCard: MaterialCardView = binding.imageCard
    private val imageView: ImageView = binding.imageView
    private val checkHint: ImageView = binding.checkHint
    init {
        imageCard.setOnClickListener {
            //to do
        }
    }
}