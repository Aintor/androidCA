package iss.nus.edu.sg.androidca

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.core.graphics.ColorUtils
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import iss.nus.edu.sg.androidca.databinding.ListItemLeaderboardBinding

class LeaderboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val rowBindings = mutableListOf<ListItemLeaderboardBinding>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val rowCornerRadius = resources.getDimension(R.dimen.button_radius)

    init {
        orientation = VERTICAL
        addHeaderRow()
        addDataRows()
    }

    private fun addHeaderRow() {
        val headerBinding = ListItemLeaderboardBinding.inflate(inflater, this, false)

        val headers = listOf(
            headerBinding.rank to "Rank",
            headerBinding.username to "Username",
            headerBinding.time to "Time"
        )

        val headerColor = ContextCompat.getColor(context, R.color.deep_retro)

        for ((view, label) in headers) {
            view.text = label
            view.typeface = getFont(context, R.font.title)
            view.setTextColor(headerColor)
        }

        addView(headerBinding.root)
    }

    private fun addDataRows() {
        for (i in 0 until 5) {
            val binding = ListItemLeaderboardBinding.inflate(inflater, this, false)
            binding.root.visibility = GONE
            addView(binding.root)
            rowBindings.add(binding)
        }
    }

    fun setData(entries: List<LeaderboardEntry>, highlightedPlayerRank: Int?) {
        for (i in 0 until 5) {
            val binding = rowBindings[i]
            if (i < entries.size) {
                val entry = entries[i]
                bindDataToRow(binding, entry, highlightedPlayerRank)
                animateRowIn(binding.root, i)
            } else {
                binding.root.visibility = GONE
            }
        }
    }

    private fun bindDataToRow(binding: ListItemLeaderboardBinding, entry: LeaderboardEntry, highlightedPlayerRank: Int?) {
        binding.rank.text = "${entry.rank}"
        binding.username.text = entry.username
        binding.time.text = entry.time.toString()

        val backgroundColor: Int
        val baseColor = ContextCompat.getColor(context, R.color.retro)
        if (entry.rank == highlightedPlayerRank) {
            backgroundColor = ColorUtils.setAlphaComponent(baseColor, (255 * 0.5).toInt())
        } else {
            backgroundColor = Color.TRANSPARENT
        }

        val shapeDrawable = MaterialShapeDrawable(
            ShapeAppearanceModel.builder().setAllCornerSizes(rowCornerRadius).build()
        ).apply {
            fillColor = ColorStateList.valueOf(backgroundColor)
        }
        binding.root.background = shapeDrawable
    }

    private fun animateRowIn(rowView: View, index: Int) {
        rowView.visibility = VISIBLE
        rowView.alpha = 0f
        rowView.translationY = 50f

        rowView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay((index * 100).toLong())
            .start()
    }
}