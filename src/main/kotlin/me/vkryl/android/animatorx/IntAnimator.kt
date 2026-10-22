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
import me.vkryl.core.fromTo

fun interface IntAnimatorListener {
  fun onValueChange(newValue: Int)
  fun onValueChangeFinished(finalValue: Int, byAnimationEnd: Boolean) { }
}

open class IntAnimator @JvmOverloads constructor(
  val duration: Long,
  val interpolator: TimeInterpolator,
  initialValue: Int = DEFAULT_INITIAL_VALUE,
  listener: IntAnimatorListener
) {
  companion object {
    const val DEFAULT_INITIAL_VALUE = 0
  }

  private var fromValue: Int = initialValue
  private var toValue: Int = initialValue

  private val fraction = FloatAnimator(
    duration, interpolator,
    initialValue = 1.0f,
    listener = object : FloatAnimatorListener {
      override fun onValueChange(newValue: Float) {
        val value = interpolate(fromValue, toValue, newValue)
        if (this@IntAnimator.value != value) {
          this@IntAnimator.value = value
          listener.onValueChange(value)
        }
      }

      override fun onValueChangeFinished(finalValue: Float, byAnimationEnd: Boolean) {
        listener.onValueChangeFinished(value, byAnimationEnd)
      }
    }
  )

  var value: Int = initialValue
    private set
  val finalValue: Int
    get() = if (isAnimating) toValue else value
  var forcedValue: Int
    get() = value
    set(newValue) = changeValue(newValue, false)
  var animatedValue: Int
    get() = value
    set(newValue) = changeValue(newValue, true)

  val isAnimating: Boolean
    get() = fraction.isAnimating

  fun stopAnimation(): Boolean =
    fraction.stopAnimation()

  @JvmOverloads
  fun changeValue(newValue: Int, animated: Boolean = true) {
    if (animated && isAnimating && toValue == newValue) {
      return
    }
    stopAnimation()
    fromValue = value
    toValue = newValue
    fraction.changeValue(0.0f, 1.0f, animated)
  }

  protected open fun interpolate(fromValue: Int, toValue: Int, fraction: Float): Int =
    fromTo(fromValue, toValue, fraction)
}