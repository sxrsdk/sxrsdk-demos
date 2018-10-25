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

package com.samsungxr.keyboard.speech;

import android.graphics.Color;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.mic.RecognitionRmsChangeListener;
import com.samsungxr.keyboard.util.Constants;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;
import com.samsungxr.utility.Log;

import java.util.ArrayList;

public class SoundWave extends SXRSceneObject implements RecognitionRmsChangeListener {

    private SXRSceneObject[] boxes;
    private ArrayList<SXRSceneObject> columns;
    private int currentBox = 0, currentColumn = 0;
    private float minRange, maxRange;
    private boolean canAnimate = false;

    public SoundWave(SXRContext gvrContext, int width, float minRange, float maxRange) {

        super(gvrContext);
        setName(SceneObjectNames.SOUND_WAVE);
        boxes = new SXRSceneObject[width * 5];
        columns = new ArrayList<SXRSceneObject>();
        this.minRange = minRange;
        this.maxRange = maxRange;

        for (int i = 0; i < width; i++)
            createColumn(gvrContext);

        currentColumn = 0;
        hide();
    }

    private void createColumn(SXRContext context) {

        SXRSceneObject column = new SXRSceneObject(context);
        column.addChildObject(createBox(context, Color.argb(1, 230, 72, 50)));
        column.addChildObject(createBox(context, Color.argb(1, 209, 60, 43)));
        column.addChildObject(createBox(context, Color.argb(1, 186, 47, 35)));
        column.addChildObject(createBox(context, Color.argb(1, 163, 34, 27)));
        column.addChildObject(createBox(context, Color.argb(1, 142, 22, 20)));
        columns.add(column);
        this.addChildObject(column);
        currentColumn++;
        currentBox = 0;
    }

    public void update(float newSize, float newPositionX) {

        float waveSize = newSize - 0.23f;
        getTransform().setPositionX(newPositionX);
        getTransform().setPositionY(0.1f);
        getTransform().setPositionZ(Constants.CAMERA_DISTANCE);
        getTransform().setScale(waveSize, 0.5f, 0.5f);
    }

    private SXRSceneObject createBox(SXRContext context, int color) {

        SXRSceneObject box1 = new SXRSceneObject(
                context,
                0.1f,
                0.1f,
                context.getAssetLoader().loadTexture(new SXRAndroidResource(context, R.drawable.soundwave_wave_block)));
        box1.getRenderData().getMaterial().setColor(color);
        box1.getRenderData().getMaterial().setOpacity(1);
        box1.getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD_SOUND_WAVE);
        box1.getTransform().setPosition(0 - 0.11f * currentColumn, 0 - 0.11f * currentBox, 0);
        boxes[currentBox] = box1;
        currentBox++;
        return box1;

    }

    private void setColumn(float amplitude) {

        float valueBlock = (maxRange - minRange) / 5;
        SXRSceneObject currentColumn = columns.get(0);

        for (int i = 0; i < columns.size(); i++) {

            columns.get(i).getTransform().setPositionX(
                    columns.get(i).getTransform().getPositionX() + 0.11f);

        }
        columns.get(0).getTransform().setPositionX(
                columns.get(0).getTransform().getPositionX() - (columns.size()) * 0.11f);

        for (int i = 0; i < columns.get(0).getChildrenCount(); i++) {
            if (valueBlock * (1 + i) <= amplitude || i == 0)
                columns.get(0).getChildByIndex(4 - i).getRenderData().getMaterial().setOpacity(1);
            else
                columns.get(0).getChildByIndex(4 - i).getRenderData().getMaterial().setOpacity(0);

        }
        columns.remove(0);
        columns.add(currentColumn);

    }

    public void enableAnimation() {

        canAnimate = true;

    }

    private void hide() {

        canAnimate = false;
        for (int i = 0; i < columns.size(); i++) {

            for (int o = 0; o < columns.get(i).getChildrenCount(); o++) {
                columns.get(i).getChildByIndex(o).getRenderData().getMaterial().setOpacity(0);
            }
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {

        if (!canAnimate)
            return;
        setColumn(rmsdB);
        Log.e(null, "" + rmsdB);
    }

    @Override
    public void onRmsEnd() {

        hide();
        Log.e(null, "END");
    }
}
