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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.controls.R;
import com.samsungxr.controls.menu.MenuControlNode;
import com.samsungxr.controls.model.Apple.Motion;
import com.samsungxr.controls.shaders.ButtonShader;
import com.samsungxr.controls.util.RenderingOrder;
import com.samsungxr.controls.util.Text;

public class MotionButton extends MenuControlNode {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private boolean select = false;

    private  int WIDTH = 0;
    private  int HEIGHT = 0;

    private float quadWidth = .91f;
    private float quadHeigth = .22f;

    private String title;
    private Motion motion;

    public MotionButton(SXRContext sxrContext, String title, Motion motion) {
        super(sxrContext);

        this.motion = motion;
        this.title = title;

        SXRMesh sMesh = getSXRContext().createQuad(quadWidth, quadHeigth);

        WIDTH = (int)(100.0f * quadWidth);
        HEIGHT = (int) (100.0f * quadHeigth);

        attachRenderData(new SXRRenderData(sxrContext));
        getRenderData().setMaterial(new SXRMaterial(sxrContext, new SXRShaderId(ButtonShader.class)));
        getRenderData().setMesh(sMesh);

        createTextures();

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_TEXT);

        attachComponent(new SXRMeshCollider(sxrContext, false));
    }

    public Motion getMotion() {
        return motion;
    }

    private void createTextures() {
        SXRAssetLoader importer = getSXRContext().getAssetLoader();
        Text text = new Text(title, Align.LEFT, 3.5f, Color.parseColor("#ffffff"), Color.parseColor("#00000000"), 45);
        String font = "fonts/samsung-if-bold.ttf";
        SXRMaterial material = getRenderData().getMaterial();
        SXRBitmapImage bitmapIdle = new SXRBitmapImage(getSXRContext());

        bitmapIdle.setFileName("motion_idle");
        bitmapIdle.setBitmap(create(getSXRContext().getContext(), WIDTH, HEIGHT, text, font));
        material.setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE,
                            importer.loadTexture(new SXRAndroidResource(getSXRContext(), R.raw.empty)));
        SXRTexture tex1 = new SXRTexture(getSXRContext());
        tex1.setImage(bitmapIdle);
        material.setTexture(ButtonShader.STATE1_TEXT_TEXTURE, tex1);

        text.textSize = 4.3f;

        SXRBitmapImage bitmapHover = new SXRBitmapImage(getSXRContext());
        bitmapHover.setFileName("motion_hover");
        bitmapHover.setBitmap(create(getSXRContext().getContext(), WIDTH, HEIGHT, text, font));
        material.setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE,
                            importer.loadTexture(new SXRAndroidResource(getSXRContext(), R.raw.empty)));
        SXRTexture tex2 = new SXRTexture(getSXRContext());
        tex2.setImage(bitmapHover);
        material.setTexture(ButtonShader.STATE2_TEXT_TEXTURE, tex2);

        text.textColor = 0xfff8DF35;
        SXRBitmapImage bitmapSelected = new SXRBitmapImage(getSXRContext());
        bitmapSelected.setFileName("motion_selected");
        bitmapSelected.setBitmap(create(getSXRContext().getContext(), WIDTH, HEIGHT, text, font));
        material.setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE,
                            importer.loadTexture(new SXRAndroidResource(getSXRContext(), R.raw.empty)));
        SXRTexture tex3 = new SXRTexture(getSXRContext());
        tex3.setImage(bitmapSelected);
        material.setTexture(ButtonShader.STATE3_TEXT_TEXTURE, tex3);
    }

    @Override
    protected void gainedFocus() {
        if (!select) {
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
        }
    }

    @Override
    protected void lostFocus() {
        if (!select) {
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        }
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
        select = true;
    }

    public void unselect() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        select = false;
    }

    public void select(){
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
        select = true;
    }

    public static Bitmap create(Context context, int width, int height, Text text, String font) {

        Resources res = context.getResources();
        float scale = res.getDisplayMetrics().density;

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), font);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setTypeface(myTypeface);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setTextSize(text.textSize * scale);
        paint.setColor(text.textColor);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawColor(text.backgroundColor);

        canvas.drawText(text.text, 0, height / 1.5f, paint);

        return bitmap;
    }
}