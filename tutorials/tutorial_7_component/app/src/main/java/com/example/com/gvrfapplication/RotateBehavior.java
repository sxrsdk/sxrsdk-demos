package com.example.org.gvrfapplication;

import com.samsungxr.SXRBehavior;
import com.samsungxr.SXRContext;

/**
 * Created by nite.luo on 3/2/2018.
 */

public class RotateBehavior extends SXRBehavior {

    private static final long TYPE_Rotate_behavior = newComponentType(RotateBehavior.class);

    float mRotationSpeed = 1.0f;

    protected RotateBehavior(SXRContext gvrContext) {
        super(gvrContext);
        mType = TYPE_Rotate_behavior;
    }

    public static long getComponentType(){ return TYPE_Rotate_behavior;}

    @Override
    public void onDrawFrame(float frameTime) {
        super.onDrawFrame(frameTime);

        getOwnerObject().getTransform().rotateByAxis(mRotationSpeed, 0,1,0);
    }
}
