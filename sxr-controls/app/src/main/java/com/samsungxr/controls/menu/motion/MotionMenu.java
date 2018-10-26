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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.controls.R;
import com.samsungxr.controls.anim.AnimationsTime;
import com.samsungxr.controls.focus.ControlNode;
import com.samsungxr.controls.menu.GridNodes;
import com.samsungxr.controls.menu.ItemSelectedListener;
import com.samsungxr.controls.menu.MenuWindow;
import com.samsungxr.controls.menu.RadioButtonNode;
import com.samsungxr.controls.menu.RadioGrupoNode;
import com.samsungxr.controls.model.Apple;
import com.samsungxr.controls.model.Apple.Motion;

public class MotionMenu extends MenuWindow {

    private final float PREVIEW_POSITION_X = -.92f;
    private final float PREVIEW_POSITION_Y = -0.63f;
    private final float PREVIEW_POSITION_Z = 0.2f;
    private final float GRID_POSITION_X = 0.33f;
    private final float GRID_POSITION_Y = -0.38f;
    private final float GRID_POSITION_Z = 0.f;

    private MenuPreview previewArea;
    private GridNodes mGrid;
    private RadioGrupoNode radioGroup;

    public MotionMenu(SXRContext sxrContext) {
        super(sxrContext);

        createPreviewBox();

        attachGrid();

        attachRadioGroup();
    }

    private void attachGrid() {

        ParserMotionItem parse = new ParserMotionItem(getSXRContext());

        mGrid = new GridNodes(getSXRContext(), parse.getList(),R.array.grid,
                new ItemSelectedListener() {

                    @Override
                    public void selected(ControlNode object) {

                        Motion motion = ((MotionButton) object).getMotion();
                        Apple.motion = motion;
                        previewArea.changeInterpolatorTo(Apple.defineInterpolator(motion));
                        previewArea.changeColorTo(Apple.getColor(getSXRContext()));
                    }
                });

        mGrid.getTransform().setPosition(GRID_POSITION_X, GRID_POSITION_Y,
                GRID_POSITION_Z);
    }

    private void createPreviewBox() {

        previewArea = new MenuPreview(getSXRContext(), getSXRContext().createQuad(1.2f, 1),
                getSXRContext().getAssetLoader().loadTexture( new SXRAndroidResource(this.getSXRContext(), R.raw.empty)));

        previewArea.getTransform().setPosition(PREVIEW_POSITION_X, PREVIEW_POSITION_Y,
                PREVIEW_POSITION_Z);

        previewArea.getRenderData().getMaterial().setOpacity(0);

        addChildObject(previewArea);
    }

    private void attachRadioGroup() {

        radioGroup =  new RadioGrupoNode(getSXRContext(), new ItemSelectedListener() {

            @Override
            public void selected(ControlNode object) {

                RadioButtonNode button = (RadioButtonNode)object;
                AnimationsTime.setDropTime(button.getSecond());

                previewArea.animationsTime();
            }
        }, 1, 3, 5);

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