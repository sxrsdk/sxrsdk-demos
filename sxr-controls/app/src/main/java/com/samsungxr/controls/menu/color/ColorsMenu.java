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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.controls.Main;
import com.samsungxr.controls.R;
import com.samsungxr.controls.anim.AnimationsTime;
import com.samsungxr.controls.anim.ColorWorm;
import com.samsungxr.controls.focus.ControlSceneObject;
import com.samsungxr.controls.menu.GridSceneObjects;
import com.samsungxr.controls.menu.ItemSelectedListener;
import com.samsungxr.controls.menu.MenuWindow;
import com.samsungxr.controls.menu.RadioButtonSceneObject;
import com.samsungxr.controls.menu.RadioGrupoSceneObject;
import com.samsungxr.controls.util.ColorControls.Color;

public class ColorsMenu extends MenuWindow {

    private final float PREVIEW_POSITION_X = -.92f;
    private final float PREVIEW_POSITION_Y = -0.72f;
    private final float PREVIEW_POSITION_Z = 0.2f;

    private final float GRID_POSITION_X = 0.28f;
    private final float GRID_POSITION_Y = -0.38f;
    private final float GRID_POSITION_Z = 0.025f;

    private MenuColorsPreview previewArea;
    private GridSceneObjects mGrid;
    private RadioGrupoSceneObject radioGroup;

    public ColorsMenu(SXRContext gvrContext) {
        super(gvrContext);

        createPreviewBox();

        attachGrid();

        attachRadioGroup();
    }

    private void attachGrid() {

        ParseColorItem parse = new ParseColorItem(getSXRContext());

        mGrid = new GridSceneObjects(getSXRContext(), parse.getList(), R.array.colors_grid,
                new ItemSelectedListener() {

                    @Override
                    public void selected(ControlSceneObject object) {

                        ColorsButton colorButton = (ColorsButton) object;
                        Color color = colorButton.getColor();
                        previewArea.changeColorTo(color);

                        ColorWorm.lastColor = Main.worm.getColor();
                        ColorWorm.currentColor = color;
                        Main.animationColor.showPlayButton();
                    }
                });

        mGrid.getTransform().setPosition(GRID_POSITION_X, GRID_POSITION_Y,
                GRID_POSITION_Z);
    }

    private void createPreviewBox() {
        previewArea = new MenuColorsPreview(getSXRContext(), getSXRContext().createQuad(1.2f, 1),
                getSXRContext().getAssetLoader().loadTexture(
                        new SXRAndroidResource(this.getSXRContext(), R.raw.empty)));

        previewArea.getTransform().setPosition(PREVIEW_POSITION_X, PREVIEW_POSITION_Y,
                PREVIEW_POSITION_Z);

        previewArea.getRenderData().getMaterial().setOpacity(0);

        addChildObject(previewArea);
    }

    private void attachRadioGroup() {

        radioGroup =  new RadioGrupoSceneObject(getSXRContext(), new ItemSelectedListener() {

            @Override
            public void selected(ControlSceneObject object) {

                RadioButtonSceneObject button = (RadioButtonSceneObject)object;
                AnimationsTime.setChangeColorTime(button.getSecond());
            }
        }, 0.2f, 0.5f, 5);

        radioGroup.getTransform().setPosition(-1.37f, -1.24f, PREVIEW_POSITION_Z);

        addChildObject(radioGroup);
    }

    @Override
    public void show() {

        radioGroup.show();

        removeChildObject(mGrid);
        removeChildObject(previewArea);

        addChildObject(mGrid);
        addChildObject(previewArea);
        previewArea.show();

        SXROpacityAnimation opacitypreviewArea = new SXROpacityAnimation(previewArea, 1f, 1);
        opacitypreviewArea.setRepeatMode(SXRRepeatMode.ONCE);
        opacitypreviewArea.start(getSXRContext().getAnimationEngine());
    }

    @Override
    public void hide() {

        radioGroup.hide();

        removeChildObject(mGrid);

        previewArea.hide();
        removeChildObject(previewArea);

        SXROpacityAnimation opacitypreviewArea = new SXROpacityAnimation(previewArea, 0.5f, 0);
        opacitypreviewArea.setRepeatMode(SXRRepeatMode.ONCE);
        opacitypreviewArea.start(getSXRContext().getAnimationEngine());
    }
}