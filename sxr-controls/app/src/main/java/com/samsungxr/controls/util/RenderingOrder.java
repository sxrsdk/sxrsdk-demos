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

package com.samsungxr.controls.util;
import com.samsungxr.SXRRenderData;

public class RenderingOrder {

    public static final int SKYBOX = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int CLOUDS = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int SUN = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int GROUND = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int WOOD = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int FENCE = SXRRenderData.SXRRenderingOrder.BACKGROUND;
    public static final int GRASS = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int FLOWERS = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int WORM = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int WORM_SHADOW_HEADER = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int WORM_SHADOW_MIDDLE = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int WORM_SHADOW_END = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    public static final int APPLE = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int APPLE_SHADOW = SXRRenderData.SXRRenderingOrder.TRANSPARENT;

    public static final int ORDER_RENDERING_GAMEPAD = SXRRenderData.SXRRenderingOrder.GEOMETRY;;
    public static final int ORDER_RENDERING_GAMEPAD_BUTTONS = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int ORDER_RENDERING_GAMEPAD_BUTTONS_EVENT = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int ORDER_RENDERING_TOUCHPAD_AROOWS = SXRRenderData.SXRRenderingOrder.GEOMETRY;;

    public static final int MENU_FRAME_BG = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int MENU_FRAME_TEXT = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int MENU_GRID_BUTTON = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int MENU_HEADER = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    public static final int MENU_HEADER_TEXT = SXRRenderData.SXRRenderingOrder.TRANSPARENT;
    
    public static final int MENU_BUTTON_COLOR = SXRRenderData.SXRRenderingOrder.GEOMETRY;
    
    public static final int MOVE_BUTON = SXRRenderData.SXRRenderingOrder.GEOMETRY;
}