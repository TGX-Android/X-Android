/*
 * This file is a part of X-Android
 * Copyright © Vyacheslav Krylov 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.vkryl.android.animatorx

import android.animation.TimeInterpolator
import android.graphics.Color
import androidx.annotation.ColorInt
import me.vkryl.core.ColorInterpolation
import me.vkryl.core.ColorInterpolator

class ArgbAnimator @JvmOverloads constructor(
  duration: Long,
  interpolator: TimeInterpolator,
  @ColorInt initialValue: Int = DEFAULT_INITIAL_VALUE,
  interpolation: ColorInterpolation = ColorInterpolation.SRGB,
  listener: IntAnimatorListener
) : IntAnimator(duration, interpolator, initialValue, listener) {
  companion object {
    const val DEFAULT_INITIAL_VALUE = Color.BLACK
  }

  private val colorInterpolator = ColorInterpolator(interpolation)

  override fun interpolate(fromValue: Int, toValue: Int, fraction: Float): Int =
    colorInterpolator.interpolate(fromValue, toValue, fraction)
}