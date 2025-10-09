package com.jetsynthesys.rightlife.ai_package.ui.eatright

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class KValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value >= 1000) {
            "${(value / 1000).toInt()}k"
        } else {
            DecimalFormat("###").format(value)
        }
    }
}
