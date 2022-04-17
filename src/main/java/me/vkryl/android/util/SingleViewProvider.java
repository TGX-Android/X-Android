/*
 * This file is a part of X-Android
 * Copyright © Vyacheslav Krylov (slavone@protonmail.ch) 2014-2022
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
 *
 * File created on 14/02/2017
 */

package me.vkryl.android.util;

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import me.vkryl.android.ViewUtils;

public class SingleViewProvider implements ViewProvider {
  private @Nullable View view;

  public SingleViewProvider (@Nullable View view) {
    this.view = view;
  }

  public void setView (@Nullable View view) {
    this.view = view;
  }

  @Override
  public @Nullable View findAnyTarget () {
    return view;
  }

  @Override
  public boolean belongsToProvider (View view) {
    return findAnyTarget() == view;
  }

  @Override
  public boolean hasAnyTargetToInvalidate () {
    return findAnyTarget() != null;
  }

  @Override
  public void invalidate () {
    View view = findAnyTarget();
    if (view != null) {
      view.invalidate();
    }
  }

  @Override
  public void invalidateParent () {
    View view = findAnyTarget();
    if (view != null) {
      ViewParent parent = view.getParent();
      if (parent != null) {
        ((View) parent).invalidate();
      }
    }
  }

  @Override
  public void invalidateParent (int left, int top, int right, int bottom) {
    View view = findAnyTarget();
    if (view != null) {
      ViewParent parent = view.getParent();
      if (parent != null) {
        ((View) parent).invalidate(left, top, right, bottom);
      }
    }
  }

  @Override
  public void invalidate (int left, int top, int right, int bottom) {
    View view = findAnyTarget();
    if (view != null) {
      view.invalidate(left, top, right, bottom);
    }
  }

  @Override
  public void invalidate (Rect dirty) {
    View view = findAnyTarget();
    if (view != null) {
      view.invalidate(dirty);
    }
  }

  @Override
  public void invalidateOutline (boolean withInvalidate) {
    View view = findAnyTarget();
    if (view != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        view.invalidateOutline();
      }
      if (withInvalidate) {
        view.invalidate();
      }
    }
  }

  @Override
  public void postInvalidate () {
    View view = findAnyTarget();
    if (view != null) {
      view.postInvalidate();
    }
  }

  @Override
  public void performClickSoundFeedback () {
    ViewUtils.onClick(findAnyTarget());
  }

  @Override
  public void requestLayout () {
    View view = findAnyTarget();
    if (view != null) {
      view.requestLayout();
    }
  }

  @Override
  public int getMeasuredWidth () {
    View view = findAnyTarget();
    return view != null ? view.getMeasuredWidth() : 0;
  }

  @Override
  public int getMeasuredHeight () {
    View view = findAnyTarget();
    return view != null ? view.getMeasuredHeight() : 0;
  }

  @Override
  public boolean invalidateContent () {
    return false;
  }
}
