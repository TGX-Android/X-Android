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

@file:Suppress("MemberVisibilityCanBePrivate")

package me.vkryl.android.animatorx

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Build

fun interface FloatAnimatorListener {
  fun onValueChange(newValue: Float)
  fun onValueChangeFinished(finalValue: Float, byAnimationEnd: Boolean) { }
}

class FloatAnimator @JvmOverloads constructor(
  val duration: Long,
  val interpolator: TimeInterpolator,
  initialValue: Float = DEFAULT_INITIAL_VALUE,
  private val listener: FloatAnimatorListener
) {
  companion object {
    const val DEFAULT_INITIAL_VALUE = 0.0f
  }

  var value: Float = initialValue
    private set
  private var fromValue: Float = initialValue
  private var toValue: Float = initialValue

  val finalValue: Float
    get() = if (isAnimating) toValue else value
  var forcedValue: Float
    get() = value
    set(newValue) = changeValue(newValue, false)
  var animatedValue: Float
    get() = value
    set(newValue) = changeValue(newValue, true)

  val isAnimating: Boolean
    get() = animator != null
  private var animator: ValueAnimator? = null

  fun stopAnimation(): Boolean {
    return animator?.let {
      it.cancel()
      animator = null
      true
    } ?: false
  }

  @JvmOverloads
  fun changeValue(newValue: Float, animated: Boolean = true) {
    if (animated && isAnimating && toValue == newValue) {
      return
    }
    stopAnimation()
    changeValue(value, newValue, animated)
  }

  internal fun changeValue(fromValue: Float, toValue: Float, animated: Boolean) {
    value = fromValue
    this.fromValue = fromValue
    this.toValue = toValue
    if (!animated || !startAnimation()) {
      applyValue(toValue)
      listener.onValueChangeFinished(toValue, false)
    }
  }

  private fun startAnimation(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ValueAnimator.areAnimatorsEnabled()) {
      return false
    }
    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.duration = duration
    animator.interpolator = interpolator
    animator.addUpdateListener {
      if (this.animator === it) {
        applyValue(fromValue + (toValue - fromValue) * it.animatedFraction)
      }
    }
    animator.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        if (this@FloatAnimator.animator === animation && stopAnimation()) {
          listener.onValueChangeFinished(value, true)
        }
      }
    })
    this.animator = animator
    animator.start()
    return true
  }

  private fun applyValue(newValue: Float) {
    if (value != newValue) {
      value = newValue
      listener.onValueChange(newValue)
    }
  }
}
