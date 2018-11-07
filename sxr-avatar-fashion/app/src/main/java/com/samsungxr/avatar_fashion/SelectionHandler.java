package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRLight;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRPointLight;

public class SelectionHandler extends SXREventListeners.TouchEvents
{
    private SXRNode mSelectionLight = null;

    public SelectionHandler(SXRContext ctx)
    {
        SXRPointLight light = new SXRPointLight(ctx);
        mSelectionLight = new SXRNode(ctx);
        light.setAmbientIntensity(0, 0, 0, 1);
        light.setDiffuseIntensity(0.7f, 0.7f, 0.5f, 1);
        light.setSpecularIntensity(0.7f, 0.7f, 0.5f, 1);
        mSelectionLight.getTransform().setPositionY(1);
        mSelectionLight.attachComponent(light);
    }

    public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
    {
        SXRNode.BoundingVolume bv = sceneObj.getBoundingVolume();
        SXRNode lightParent = mSelectionLight.getParent();
        SXRNode pickedParent = sceneObj.getParent();

        mSelectionLight.getTransform().setPositionY(bv.radius);
        if (lightParent == pickedParent)
        {
            SXRLight light = mSelectionLight.getLight();
            light.setEnable(true);
            return;
        }
        if (lightParent != null)
        {
            lightParent.removeChildObject(mSelectionLight);
        }
        pickedParent.addChildObject(mSelectionLight);
    }

    public void onExit(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
    {
        SXRLight light = mSelectionLight.getLight();
        light.setEnable(false);
    }
};
