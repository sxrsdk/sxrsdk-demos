package com.samsungxr.immersivepedia.focus;

import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRNode;
import com.samsungxr.immersivepedia.GazeController;
import com.samsungxr.utility.Log;

public class PickHandler extends SXREventListeners.PickEvents
{
    public SXRNode PickedObject = null;

    public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
    {
        if (sceneObj instanceof FocusableNode)
        {
            FocusableNode fo = (FocusableNode) sceneObj;
            PickedObject = fo;
            Log.v("PICKER", sceneObj.getName() + " onEnter");
            fo.setFocus(true);
            fo.dispatchInFocus();
            fo.hitLocation = pickInfo.getHitLocation();
        }
    }

    public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) {
        if (sceneObj instanceof FocusableNode)
        {
            FocusableNode fo = (FocusableNode) sceneObj;
            fo.dispatchInFocus();
        }
    }

    public void onExit(SXRNode sceneObj)
    {
        FocusableNode fo = (FocusableNode) PickedObject;
        if (fo != null)
        {
            Log.v("PICKER", fo.getName() + " onExit");
            fo.setFocus(false);
        }
    }

    public void onNoPick(SXRPicker picker)
    {
        FocusableNode fo = (FocusableNode) PickedObject;
        if (fo != null)
        {
            fo.setFocus(false);
            Log.v("PICKER", fo.getName() + " onNoPick");
        }
        PickedObject = null;
        GazeController.get().disableInteractiveCursor();
    }

}