package com.theost.workchat.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import com.theost.workchat.R

class DialogLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var avatar: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    init {
        inflate(context, R.layout.dialog_layout, this)

        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DialogLayout,
            defStyleAttr,
            defStyleRes
        )
        avatar = typedArray.getResourceId(R.styleable.DialogLayout_avatar, 0)
        typedArray.recycle()

        if (avatar != 0) {
            val avatarImageView = getChildAt(0) as ImageView
            avatarImageView.setImageResource(avatar)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val avatarImageView = getChildAt(0)
        val messageLayout = getChildAt(1)
        val emojiLayout = getChildAt(2)

        var totalWidth = 0
        var totalHeight = 0

        measureChildWithMargins(
            avatarImageView,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            0
        )

        // Support margin
        val avatarMargin = (avatarImageView.layoutParams as MarginLayoutParams)

        totalWidth = maxOf(totalWidth, avatarImageView.measuredWidth + avatarMargin.rightMargin)
        totalHeight = maxOf(
            totalHeight,
            avatarMargin.topMargin + avatarImageView.measuredHeight + avatarMargin.bottomMargin
        )

        measureChildWithMargins(
            messageLayout,
            widthMeasureSpec,
            totalWidth,
            heightMeasureSpec,
            0
        )

        // Support margin
        val messageMargin = (messageLayout.layoutParams as MarginLayoutParams)

        totalWidth += messageMargin.leftMargin + messageLayout.measuredWidth
        totalHeight = maxOf(totalHeight, messageLayout.measuredHeight + messageMargin.bottomMargin)

        measureChildWithMargins(
            emojiLayout,
            widthMeasureSpec,
            avatarImageView.measuredWidth,
            heightMeasureSpec,
            totalHeight
        )

        // Support margin
        val emojiMargin = (emojiLayout.layoutParams as MarginLayoutParams)

        totalWidth = maxOf(totalWidth, avatarImageView.measuredWidth + avatarMargin.rightMargin + messageMargin.leftMargin + emojiLayout.measuredWidth)
        totalHeight += emojiMargin.topMargin + emojiLayout.measuredHeight

        val resultWidth = resolveSize(paddingLeft + totalWidth + paddingRight, widthMeasureSpec)
        val resultHeight = resolveSize(paddingTop + totalHeight + paddingBottom, heightMeasureSpec)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val avatarImageView = getChildAt(0)
        val messageLayout = getChildAt(1)
        val emojiLayout = getChildAt(2)

        // Support margin
        val avatarMargin = (avatarImageView.layoutParams as MarginLayoutParams)
        val messageMargin = (messageLayout.layoutParams as MarginLayoutParams)
        val emojiMargin = (emojiLayout.layoutParams as MarginLayoutParams)

        avatarImageView.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + avatarImageView.measuredWidth,
            paddingTop + avatarImageView.measuredHeight
        )

        messageLayout.layout(
            avatarImageView.right + avatarMargin.rightMargin + messageMargin.leftMargin,
            paddingTop,
            avatarImageView.right + avatarMargin.rightMargin + messageMargin.leftMargin + messageLayout.measuredWidth,
            paddingTop + messageLayout.measuredHeight
        )

        emojiLayout.layout(
            avatarImageView.right + avatarMargin.rightMargin + messageMargin.leftMargin,
            messageLayout.bottom + messageMargin.bottomMargin + emojiMargin.topMargin,
            avatarImageView.right + avatarMargin.rightMargin + messageMargin.leftMargin + emojiLayout.measuredWidth,
            messageLayout.bottom + messageMargin.bottomMargin + emojiMargin.topMargin + emojiLayout.measuredHeight
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

}