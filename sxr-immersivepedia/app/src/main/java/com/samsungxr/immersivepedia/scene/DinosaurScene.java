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

package com.samsungxr.immersivepedia.scene;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.immersivepedia.GazeController;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.dinosaur.DinosaurFactory;
import com.samsungxr.immersivepedia.model.GalleryDinosaurGroup;
import com.samsungxr.immersivepedia.model.RotateDinosaurGroup;
import com.samsungxr.immersivepedia.model.TextDinosaurGroup;
import com.samsungxr.immersivepedia.model.VideoDinosaurGroup;
import com.samsungxr.immersivepedia.util.FPSCounter;

import java.io.IOException;

public class DinosaurScene extends SXRScene {

    public static final float CAMERA_Y = 1.6f;

    private VideoDinosaurGroup videoDinosaur;
    private GalleryDinosaurGroup galleryDinosaur = null;
    private SXRContext sxrContext;
    private TextDinosaurGroup textDinosaur;

    private RotateDinosaurGroup rotateDinosaur;

    public DinosaurScene(SXRContext sxrContext) throws IOException {
        super(sxrContext);
        this.sxrContext = sxrContext;
        DinosaurFactory.getInstance(sxrContext);
        getMainCameraRig().getTransform().setPositionY(CAMERA_Y);

        createVideoDinosauGroup(); // TRex
        createTextDinosaurGroup(); // Ankylosaurus
        createRotateDinosaurGroup(); // Styracosaurus
        createGalleryDinosaurGroup(); // Apatosaurus

        addNode(createSkybox()); //

        hide();
        addNode(createBlueSkybox()); //
    }

    private void createRotateDinosaurGroup() throws IOException {
        rotateDinosaur = new RotateDinosaurGroup(getSXRContext(), this);
        addNode(rotateDinosaur);
    }

    private void createTextDinosaurGroup() throws IOException {

        textDinosaur = new TextDinosaurGroup(getSXRContext(), this);

        textDinosaur.getTransform().setPositionZ(-DinosaurFactory.ANKYLOSAURUS_DISTANCE);

        textDinosaur.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.ANKYLOSAURUS_ANGLE_AROUND_CAMERA, 0, 1, 0, 0, 0, 0);

        addNode(textDinosaur);
    }

    private void createVideoDinosauGroup() throws IOException {

        videoDinosaur = new VideoDinosaurGroup(getSXRContext(), this);

        videoDinosaur.getTransform().setPositionZ(-DinosaurFactory.TREX_DISTANCE);

        videoDinosaur.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.TREX_ANGLE_AROUND_CAMERA, 0, 1, 0, 0, 0, 0);

        addNode(videoDinosaur);
    }

    private void createGalleryDinosaurGroup() throws IOException {
        galleryDinosaur = new GalleryDinosaurGroup(sxrContext, this);
        addNode(galleryDinosaur);
    }

    public void onStep() {
        FPSCounter.tick();
        if (this.videoDinosaur != null) {
            this.videoDinosaur.onStep();
        }
    }

    private SXRNode.ComponentVisitor showAnimator = new SXRNode.ComponentVisitor() {
        @Override
        public boolean visit(SXRComponent comp) {
            SXRRenderData rd = (SXRRenderData) comp;
            SXRNode owner = rd.getOwnerObject();
            try {
                new SXROpacityAnimation(owner, 1f, 1f).start(getSXRContext().getAnimationEngine());
            } catch (UnsupportedOperationException ex) {
                // shader doesn't support opacity
            }
            return true;
        }
    };

    private SXRNode.ComponentVisitor hideAll = new SXRNode.ComponentVisitor() {
        @Override
        public boolean visit(SXRComponent comp) {
            SXRRenderData rd = (SXRRenderData) comp;
            SXRMaterial mtl = rd.getMaterial();

            if (mtl.hasUniform("u_opacity")) {
                rd.getMaterial().setOpacity(0.0f);
            } else if (mtl.hasUniform("diffuse_color")) {
                float[] c = mtl.getDiffuseColor();
                mtl.setDiffuseColor(c[0], c[1], c[2], 0.0f);
            }
            return true;
        }
    };

    public void show() {
        GazeController.get().enableGaze();
        getRoot().forAllComponents(showAnimator, SXRRenderData.getComponentType());
    }

    public void hide() {
        getRoot().forAllComponents(hideAll, SXRRenderData.getComponentType());
    }

    private SXRNode createSkybox() {
        SXRAssetLoader loader = getSXRContext().getAssetLoader();
        SXRMesh mesh = loader.loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.environment_walls_mesh));
        SXRTexture texture = loader.loadTexture(new
                SXRAndroidResource(sxrContext, R.raw.environment_walls_tex_diffuse));
        final SXRNode skybox = new SXRNode(getSXRContext(), mesh, texture);

        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getRenderData().setRenderingOrder(0);

        SXRMesh meshGround = loader.loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.environment_ground_mesh));
        SXRTexture textureGround = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.raw.environment_ground_tex_diffuse));
        final SXRNode skyboxGround = new SXRNode(getSXRContext(), meshGround, textureGround);

        skyboxGround.getRenderData().setRenderingOrder(0);

        SXRMesh meshFx = loader.loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.windows_fx_mesh));
        SXRTexture textureFx = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.windows_fx_tex_diffuse));
        SXRNode skyboxFx = new SXRNode(getSXRContext(), meshFx, textureFx);
        skyboxGround.getRenderData().setRenderingOrder(0);

        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);

        return skybox;
    }

    private SXRNode createBlueSkybox() {

        SXRMesh mesh = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.skybox_mesh));
        SXRTexture texture = getSXRContext().getAssetLoader().loadTexture(new
                SXRAndroidResource(getSXRContext(), R.drawable.dino_skybox_tex_diffuse));
        SXRNode skybox = new SXRNode(getSXRContext(), mesh, texture);
        skybox.getTransform().setScale(1, 1, 1);
        skybox.getRenderData().setRenderingOrder(0);
        return skybox;
    }

    public void closeObjectsInScene() {
        if (galleryDinosaur.isOpen()) {
            galleryDinosaur.closeThis();
        }
        if (textDinosaur.isOpen()) {
            textDinosaur.closeAction();
        }
        if (videoDinosaur.isOpen()) {
            videoDinosaur.closeAction();
        }
    }

    public void onPause() {
        if (rotateDinosaur.isPlayed) {
            rotateDinosaur.pauseAnimation();
        }
        if (null != videoDinosaur) {
            videoDinosaur.pauseVideo();
        }
    }
}
