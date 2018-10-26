package com.samsungxr.immersivepedia.focus;

import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.immersivepedia.GazeController;
import com.samsungxr.utility.Log;

public class PickHandler extends SXREventListeners.PickEvents
{
    public SXRSceneObject PickedObject = null;

    public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo)
    {
        if (sceneObj instanceof FocusableSceneObject)
        {
            FocusableSceneObject fo = (FocusableSceneObject) sceneObj;
            PickedObject = fo;
            Log.v("PICKER", sceneObj.getName() + " onEnter");
            fo.setFocus(true);
            fo.dispatchInFocus();
            fo.hitLocation = pickInfo.getHitLocation();
        }
    }

    public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
        if (sceneObj instanceof FocusableSceneObject)
        {
            FocusableSceneObject fo = (FocusableSceneObject) sceneObj;
            fo.dispatchInFocus();
        }
    }

    public void onExit(SXRSceneObject sceneObj)
    {
        FocusableSceneObject fo = (FocusableSceneObject) PickedObject;
        if (fo != null)
        {
            Log.v("PICKER", fo.getName() + " onExit");
            fo.setFocus(false);
        }
    }

    public void onNoPick(SXRPicker picker)
    {
        FocusableSceneObject fo = (FocusableSceneObject) PickedObject;
        if (fo != null)
        {
            fo.setFocus(false);
            Log.v("PICKER", fo.getName() + " onNoPick");
        }
        PickedObject = null;
        GazeController.get().disableInteractiveCursor();
    }

}