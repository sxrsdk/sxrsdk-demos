package com.gearvrf.fasteater;

import com.samsungxr.SXRHybridObject;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by siva.penke on 7/31/2016.
 */
public class ImageFrameAnimation extends SXRAnimation {
    private final List<Future<SXRTexture>> animationTextures;
    private int lastFileIndex = -1;

    /**
     * @param material             {@link SXRMaterial} to animate
     * @param duration             The animation duration, in seconds.
     * @param texturesForAnimation arrayList of SXRTexture used during animation
     */
    public ImageFrameAnimation(SXRMaterial material, float duration,
                                  final List<Future<SXRTexture>> texturesForAnimation) {
        super(material, duration);
        animationTextures = texturesForAnimation;
    }

    @Override
    protected void animate(SXRHybridObject target, float ratio) {
        final int size = animationTextures.size();
        final int fileIndex = (int) (ratio * size);

        if (lastFileIndex == fileIndex || fileIndex == size) {
            return;
        }

        lastFileIndex = fileIndex;

        SXRMaterial material = (SXRMaterial) target;
        material.setMainTexture(animationTextures.get(fileIndex));
    }
}
