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

public class HtmlTag {
  public final String openTag, closeTag;

  public HtmlTag (String openTag, String closeTag) {
    this.openTag = openTag;
    this.closeTag = closeTag;
  }

  public HtmlTag (String tagName) {
    this("<" + tagName + ">", "</" + tagName + ">");
  }
}
