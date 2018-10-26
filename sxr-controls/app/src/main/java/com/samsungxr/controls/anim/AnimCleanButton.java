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

package com.samsungxr.controls.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
import com.samsungxr.controls.shaders.ButtonShader;
import com.samsungxr.controls.shaders.ColorSwapShader;
import com.samsungxr.controls.util.RenderingOrder;
import com.samsungxr.controls.util.Text;

public class AnimCleanButton extends MenuControlNode {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private int WIDTH = 0;
    private int HEIGHT = 0;

    private float quadWidth = .4f;
    private float quadHeigth = .2f;

    private String title;

    public AnimCleanButton(SXRContext sxrContext, String title) {
        super(sxrContext);

        this.title = title;

        SXRMesh sMesh = getSXRContext().createQuad(quadWidth, quadHeigth);

        WIDTH = (int) (100.0f * quadWidth);
        HEIGHT = (int) (100.0f * quadHeigth);

        attachRenderData(new SXRRenderData(sxrContext));
        getRenderData().setMaterial(
                new SXRMaterial(sxrContext, new SXRShaderId(ButtonShader.class)));
        getRenderData().setMesh(sMesh);

        createTextures();

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MOVE_BUTON);

        attachComponent(new SXRMeshCollider(sxrContext, false));

    }

    private void createTextures() {
        SXRAssetLoader importer = getSXRContext().getAssetLoader();
        Text text = new Text(title, Align.CENTER, 3f, Color.parseColor("#000000"), Color.parseColor("#ffffff"), 45);
        String font = "fonts/samsung-if-bold.ttf";
        SXRMaterial material = getRenderData().getMaterial();
        SXRBitmapImage bitmapIdle = new SXRBitmapImage(getSXRContext());

        bitmapIdle.setFileName("anim_idle");
        bitmapIdle.setBitmap(create(getSXRContext().getContext(), WIDTH, HEIGHT, text, font));
        material.setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE,
                            importer.loadTexture(new SXRAndroidResource(getSXRContext(), R.raw.empty)));
        SXRTexture tex = new SXRTexture(getSXRContext());
        tex.setImage(bitmapIdle);
        material.setTexture(ButtonShader.STATE1_TEXT_TEXTURE, tex);
        text.textSize = 3.3f;

        SXRBitmapImage bitmapHover = new SXRBitmapImage(getSXRContext());
        bitmapHover.setFileName("anim_hover");
        bitmapHover.setBitmap(create(getSXRContext().getContext(), WIDTH, HEIGHT, text, font));
        material.setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE,
                            importer.loadTexture(new SXRAndroidResource(getSXRContext(), R.raw.empty)));
        SXRTexture tex1 = new SXRTexture(getSXRContext());
        tex1.setImage(bitmapHover);
        material.setTexture(ButtonShader.STATE2_TEXT_TEXTURE, tex1);

        SXRBitmapImage bitmapSelected = new SXRBitmapImage(getSXRContext());
        bitmapSelected.setFileName("anim_selected");
        bitmapSelected.setBitmap(create(getSXRContext().getContext(), WIDTH, HEIGHT, text, font));
        material.setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE,
                            importer.loadTexture(new SXRAndroidResource(getSXRContext(), R.raw.empty)));
        SXRTexture tex2 = new SXRTexture(getSXRContext());
        tex2.setImage(bitmapSelected);
        material.setTexture(ButtonShader.STATE3_TEXT_TEXTURE, tex2);
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
    }

    public static Bitmap create(Context context, int width, int height, Text text, String font) {

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), font);

        Resources res = context.getResources();
        float scale = res.getDisplayMetrics().density;

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        final Paint paint2 = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint2.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint2.setColor(text.backgroundColor);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint2);

        paint2.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint2);

        Paint paint = new Paint();
        paint.setTypeface(myTypeface/*
                                     * Typeface.create(Typeface.DEFAULT,
                                     * Typeface.BOLD)
                                     */);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setTextSize(text.textSize * scale);
        paint.setFakeBoldText(true);
        paint.setColor(text.textColor);
        paint.setFilterBitmap(true);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawText(text.text, width / 2 - rectText.exactCenterX(),
                height / 1.5f /*- rectText.exactCenterY()*/, paint);

        return bitmap;
    }

    @Override
    public void unselect() {
    }

    @Override
    public void select() {
    }
}
