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
 * File created on 13/02/2017
 */

package me.vkryl.android.util;

import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.vkryl.android.ViewUtils;
import me.vkryl.core.reference.ReferenceUtils;

public class MultipleViewProvider implements ViewProvider {
  public interface InvalidateContentProvider {
    void invalidateContent ();
  }

  private static final int ACTION_INVALIDATE_ALL = 0;
  private static final int ACTION_REQUEST_LAYOUT = 1;
  private final Handler handler;

  public MultipleViewProvider () {
    this.handler = new Handler(Looper.getMainLooper(), msg -> {
      switch (msg.what) {
        case ACTION_INVALIDATE_ALL:
          invalidate();
          break;
        case ACTION_REQUEST_LAYOUT:
          ((View) msg.obj).requestLayout();
          break;
      }
      return true;
    });
  }

  private @Nullable InvalidateContentProvider contentProvider;

  public MultipleViewProvider setContentProvider (@Nullable InvalidateContentProvider contentProvider) {
    this.contentProvider = contentProvider;
    return this;
  }

  private @Nullable List<Reference<View>> views;

  public @Nullable List<Reference<View>> getViewsList () {
    return views;
  }

  public final boolean attachToView (@Nullable View view) {
    if (view != null) {
      if (views == null) {
        views = new ArrayList<>(2);
      }

      if (views.isEmpty()) {
        views.add(new WeakReference<>(view));
        return true;
      }

      boolean found = false;
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View currentView = views.get(i).get();
        if (currentView == null) {
          views.remove(i);
        } else if (currentView.equals(view)) {
          found = true;
        }
      }

      if (!found) {
        views.add(new WeakReference<>(view));
        return true;
      }
    } else {
      ReferenceUtils.gcReferenceList(views);
    }

    return false;
  }

  public final boolean detachFromView (@Nullable View view) {
    if (views != null) {
      if (view != null) {
        boolean found = false;
        final int size = views.size();
        for (int i = size - 1; i >= 0; i--) {
          View currentView = views.get(i).get();
          if (currentView == null) {
            views.remove(i);
          } else if (currentView.equals(view)) {
            views.remove(i);
            found = true;
          }
        }
        return found;
      } else {
        ReferenceUtils.gcReferenceList(views);
      }
    }
    return false;
  }

  public final int detachFromAllViews () {
    if (views != null) {
      final int size = views.size();
      int removedCount = 0;
      for (int i = size - 1; i >= 0; i--) {
        View currentView = views.get(i).get();
        views.remove(i);
        if (currentView != null) {
          removedCount++;
        }
      }
      return removedCount;
    }
    return 0;
  }

  @Override
  public void invalidateParent (int left, int top, int right, int bottom) {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          ViewParent parent = view.getParent();
          if (parent != null) {
            ((View) parent).invalidate(left, top, right, bottom);
          }
        } else {
          views.remove(i);
        }
      }
    }
  }

  @Override
  public void invalidateParent () {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          ViewParent parent = view.getParent();
          if (parent != null) {
            ((View) parent).invalidate();
          }
        } else {
          views.remove(i);
        }
      }
    }
  }

  @Override
  public void invalidate () {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          view.invalidate();
        } else {
          views.remove(i);
        }
      }
    }
  }

  @Override
  public void invalidateOutline (boolean withInvalidate) {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.invalidateOutline();
          }
          if (withInvalidate) {
            view.invalidate();
          }
        } else {
          views.remove(i);
        }
      }
    }
  }

  @Override
  public void invalidate (int left, int top, int right, int bottom) {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          view.invalidate(left, top, right, bottom);
        } else {
          views.remove(i);
        }
      }
    }
  }

  @Override
  public void invalidate (Rect dirty) {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          view.invalidate(dirty);
        } else {
          views.remove(i);
        }
      }
    }
  }

  @Override
  public boolean belongsToProvider (View childView) {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          if (view != childView)
            continue;
          return true;
        }
        views.remove(i);
      }
    }
    return false;
  }

  @Override
  public @Nullable View findAnyTarget () {
    if (views != null) {
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          return view;
        }
        views.remove(i);
      }
    }
    return null;
  }

  @Override
  public boolean hasAnyTargetToInvalidate () {
    return views != null && !views.isEmpty();
  }

  @Override
  public void postInvalidate () {
    handler.sendMessage(handler.obtainMessage(ACTION_INVALIDATE_ALL));
  }

  @Override
  public void performClickSoundFeedback () {
    View view = findAnyTarget();
    if (view != null) {
      ViewUtils.onClick(view);
    }
  }

  @Override
  public void requestLayout () {
    if (views != null) {
      boolean isBackground = Looper.myLooper() != Looper.getMainLooper();
      final int size = views.size();
      for (int i = size - 1; i >= 0; i--) {
        View view = views.get(i).get();
        if (view != null) {
          if (isBackground) {
            handler.sendMessage(handler.obtainMessage(ACTION_REQUEST_LAYOUT, view));
          } else {
            view.requestLayout();
          }
        } else {
          views.remove(i);
        }
      }
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
    if (contentProvider != null) {
      contentProvider.invalidateContent();
      return true;
    }
    return false;
  }
}
