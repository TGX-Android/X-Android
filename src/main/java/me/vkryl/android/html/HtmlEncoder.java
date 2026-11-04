/*
 * This file is a part of X-Android
 * Copyright © Vyacheslav Krylov 2014
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
 * File created on 11/09/2022, 12:41.
 */

package me.vkryl.android.html;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.inputmethod.BaseInputConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.vkryl.core.StringUtils;

public class HtmlEncoder {
  public interface SpanHandler<T> {
    HtmlTag[] toHtmlTag (T span);
  }

  public static class EncodeResult {
    public final String htmlText;
    public final int tagCount;

    public EncodeResult (String htmlText, int tagCount) {
      this.htmlText = htmlText;
      this.tagCount = tagCount;
    }
  }

  public static <T> EncodeResult toHtml (CharSequence charSequence, Class<T> spanKind, SpanHandler<T> spanHandler) {
    return toHtml(charSequence, 0, charSequence.length(), spanKind, spanHandler);
  }

  public static <T> EncodeResult toHtml (CharSequence charSequence, int start, int end, Class<T> spanKind, SpanHandler<T> spanHandler) {
    if (!(charSequence instanceof Spanned)) {
      String text = (start != 0 || end != charSequence.length()) ?
        charSequence.subSequence(start, end).toString() :
        charSequence.toString();
      return new EncodeResult(text, 0);
    }
    SpannableStringBuilder text = new SpannableStringBuilder(charSequence);
    BaseInputConnection.removeComposingSpans(text);

    int tagCount = 0;
    StringBuilder out = new StringBuilder();
    List<T> openSpans = new ArrayList<>();
    List<HtmlTag[]> openTags = new ArrayList<>();
    int next;
    for (int i = start; i < end; i = next) {
      next = text.nextSpanTransition(i, end, spanKind);
      T[] spans = text.getSpans(i, next, spanKind);
      List<T> runSpans = new ArrayList<>();
      List<HtmlTag[]> runTags = new ArrayList<>();
      if (spans != null && spans.length > 0) {
        Arrays.sort(spans, (a, b) -> {
          int byStart = Integer.compare(
            text.getSpanStart(a),
            text.getSpanStart(b)
          );
          int byEnd = Integer.compare(
            text.getSpanEnd(b),
            text.getSpanEnd(a)
          );
          return byStart != 0 ? byStart : byEnd;
        });
        for (T span : spans) {
          if (text.getSpanStart(span) == text.getSpanEnd(span)) {
            continue;
          }
          HtmlTag[] tags = spanHandler.toHtmlTag(span);
          if (tags != null && tags.length > 0) {
            runSpans.add(span);
            runTags.add(tags);
          }
        }
      }
      int keepCount = 0;
      while (keepCount < openSpans.size() && keepCount < runSpans.size() &&
        openSpans.get(keepCount) == runSpans.get(keepCount)) {
        keepCount++;
      }
      closeTags(out, openSpans, openTags, keepCount);
      for (int spanIndex = keepCount; spanIndex < runSpans.size(); spanIndex++) {
        HtmlTag[] tags = runTags.get(spanIndex);
        for (HtmlTag tag : tags) {
          tagCount++;
          out.append(tag.openTag);
        }
        openSpans.add(runSpans.get(spanIndex));
        openTags.add(tags);
      }
      withinStyle(out, text, i, next);
    }
    closeTags(out, openSpans, openTags, 0);
    return new EncodeResult(out.toString(), tagCount);
  }

  private static <T> void closeTags (StringBuilder out, List<T> openSpans, List<HtmlTag[]> openTags,
                                     int keepCount) {
    for (int spanIndex = openSpans.size() - 1; spanIndex >= keepCount; spanIndex--) {
      HtmlTag[] tags = openTags.remove(spanIndex);
      openSpans.remove(spanIndex);
      for (int tagIndex = tags.length - 1; tagIndex >= 0; tagIndex--) {
        if (!StringUtils.isEmpty(tags[tagIndex].closeTag)) {
          out.append(tags[tagIndex].closeTag);
        }
      }
    }
  }

  // Copy of:
  // https://android.googlesource.com/platform/frameworks/base/+/f63f20af/core/java/android/text/Html.java#636
  private static void withinStyle (StringBuilder out, CharSequence text,
                                   int start, int end) {
    for (int i = start; i < end; i++) {
      char c = text.charAt(i);

      if (c == '\n') {
        // modified: treat all new lines as <br/> tag
        out.append("<br/>");
      } else if (c == '<') {
        out.append("&lt;");
      } else if (c == '>') {
        out.append("&gt;");
      } else if (c == '&') {
        out.append("&amp;");
      } else if (c >= 0xD800 && c <= 0xDFFF) {
        if (c < 0xDC00 && i + 1 < end) {
          char d = text.charAt(i + 1);
          if (d >= 0xDC00 && d <= 0xDFFF) {
            i++;
            int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
            out.append("&#").append(codepoint).append(";");
          }
        }
      } else if (c > 0x7E || c < ' ') {
        out.append("&#").append((int) c).append(";");
      } else if (c == ' ') {
        while (i + 1 < end && text.charAt(i + 1) == ' ') {
          out.append("&nbsp;");
          i++;
        }

        out.append(' ');
      } else {
        out.append(c);
      }
    }
  }
}
