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
package com.samsungxr.controls.menu.motion;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.samsungxr.SXRContext;
import com.samsungxr.controls.R;
import com.samsungxr.controls.menu.MenuControlNode;
import com.samsungxr.controls.model.Apple.Motion;

import java.util.ArrayList;
import java.util.List;

public class ParserMotionItem {

    public TypedArray grid;
    private Resources res;
    private List<MenuControlNode> listItens = new ArrayList<MenuControlNode>();

    public ParserMotionItem(SXRContext sxrContext) {

        res = sxrContext.getContext().getResources();
        grid = res.obtainTypedArray(R.array.menu_motion_item);

        for(int i = 0; i < grid.length(); i ++){

            String title = grid.getString(i);
            Motion motion = getMotionForTitle(title);

            MotionButton button = new MotionButton(sxrContext, title, motion);
            listItens.add(button);
        }
    }

    public ArrayList<MenuControlNode> getList(){
        return (ArrayList<MenuControlNode>) listItens;
    }

    public Motion getMotionForTitle(String title){

        if(title.equals("Linear")){

            return Motion.Linear;

        } else if(title.equals("Quad in")){

            return Motion.QuadIn;

        } else if(title.equals("Expo in")){

            return Motion.ExpoIn;
        } else if(title.equals("Circular in")){

            return Motion.CircularIn;

        } else if(title.equals("Bouncing")){

            return Motion.Bouncing;

        } else if(title.equals("Quad out")){

            return Motion.QuadOut;

        } else if(title.equals("Expo out")){

            return Motion.ExpoOut;

        } else if(title.equals("Circular out")){

            return Motion.CircularOut;
        } else {
            return null;
        }
    }
}