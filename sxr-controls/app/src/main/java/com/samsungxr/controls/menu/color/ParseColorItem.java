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

package com.samsungxr.controls.menu.color;

import com.samsungxr.SXRContext;
import com.samsungxr.controls.R;
import com.samsungxr.controls.menu.MenuControlNode;
import com.samsungxr.controls.util.ColorControls;
import com.samsungxr.controls.util.ColorControls.Color;

import java.util.ArrayList;
import java.util.List;

public class ParseColorItem {

    private List<MenuControlNode> listItens = new ArrayList<MenuControlNode>();

    public ParseColorItem(SXRContext sxrContext) {

        ColorControls color = new ColorControls(sxrContext.getContext());
        
        List<Color> colorList = color.parseColorArray(R.array.worm_colors);
        
        for(Color c : colorList){
            ColorsButton button = new ColorsButton(sxrContext, c);
            listItens.add(button);
        }
    }

    public ArrayList<MenuControlNode> getList() {
        return (ArrayList<MenuControlNode>) listItens;
    }
}