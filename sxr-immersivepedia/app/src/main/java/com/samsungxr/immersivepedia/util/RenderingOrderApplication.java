/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.immersivepedia.util;

import com.samsungxr.SXRRenderData;

public class RenderingOrderApplication {

    public static final int BUTTON_BOARD = SXRRenderData.SXRRenderingOrder.OVERLAY;
    public static final int PLAY_BUTTON = SXRRenderData.SXRRenderingOrder.OVERLAY;
    public static final int LOADING_COMPONENT = SXRRenderData.SXRRenderingOrder.OVERLAY;
    public static final int GALLERY = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int GALLERY_PHOTO = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int TEXT_BACKGROUND = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int DINOSAUR = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int GALLERY_SCROLLBAR = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int TOTEM = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int CURSOR = SXRRenderData.SXRRenderingOrder.OVERLAY + 10;

    public static final int SKYBOX = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int BACKGROUND_IMAGE = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int MAIN_IMAGE = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int IMAGE_TEXT_BACKGROUND = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int IMAGE_TEXT = SXRRenderData.SXRRenderingOrder.OVERLAY;
}
