package com.jetsynthesys.rightlife

import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat

fun EditText.setHintSize(
    text: String,
    sizeSp: Int,
    fontRes: Int = R.font.dmsans_regular
) {
    val typeface = ResourcesCompat.getFont(context, fontRes)

    val span = SpannableString(text)

    // Hint size
    span.setSpan(
        AbsoluteSizeSpan(sizeSp, true),
        0,
        text.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Hint font
    typeface?.let {
        span.setSpan(
            CustomTypefaceSpan(it),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    hint = span
}
