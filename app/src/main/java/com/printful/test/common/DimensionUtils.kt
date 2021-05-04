package com.printful.test.common

import android.content.res.Resources
import androidx.core.math.MathUtils

val Int.dp: Int get() = this.toFloat().dp
val Float.dp: Int get() = (Resources.getSystem().displayMetrics.density * this).let(::roundAndClampDp)

fun roundAndClampDp(dp: Float) = when {
    dp < 0 -> MathUtils.clamp(dp.toInt(), Int.MIN_VALUE, -1)
    else -> MathUtils.clamp(dp.toInt(), 1, Int.MAX_VALUE)
}