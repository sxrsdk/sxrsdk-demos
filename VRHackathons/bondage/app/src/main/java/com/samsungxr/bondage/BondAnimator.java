package com.samsungxr.bondage;

import com.samsungxr.SXRBehavior;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTransform;
import com.samsungxr.IPickEvents;
import com.samsungxr.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;

class BondAnimator extends SXRBehavior implements IPickEvents
{
    static private long TYPE_BOND_ANIMATOR = newComponentType(BondAnimator.class);
    static final public float VELOCITY = 0.1f;
    private float           mMinDist;
    private float           mMaxDist;
    private float           mCurDist;
    private String          mElementName;
    private Vector3f        mTargetPos = new Vector3f(0, 0, 0);
    private Vector3f        mCurPos = new Vector3f(0, 0, 0);
    private SXRSceneObject  mClosest = null;
    private SXRSceneObject  mTarget = null;
    private HashMap<String, String> mMoleculeMap;
    private SoundEffect mGoodSound;
    private SoundEffect mBadSound;

    public boolean         WrongAnswer = false;

    BondAnimator(SXRContext ctx, HashMap<String, String> moleculeMap, SoundEffect good, SoundEffect bad)
    {
        super(ctx);
        mMoleculeMap = moleculeMap;
        mGoodSound = good;
        mBadSound = bad;
        mType = TYPE_BOND_ANIMATOR;
    }

    static public long getComponentType() { return TYPE_BOND_ANIMATOR; }

    public void setTarget(SXRSceneObject target)
    {
        mTarget = target;
        mElementName = getElementName(target);
        WrongAnswer = false;
    }

    SXRSceneObject getTarget()
    {
        return mTarget;
    }

    public SXRSceneObject getBondPoint(SXRSceneObject srcObj)
    {
        String name = srcObj.getName();
        int i = name.indexOf("_");
        if (i <= 0)
        {
            return null;
        }
        name = name.substring(0, i);
        String partners = mMoleculeMap.get(name);
        if (partners != null)
        {
            i = partners.indexOf(mElementName);
            if (i >= 0)
            {
                String objName = partners.substring(i);
                int j = objName.indexOf(" ");
                if (j > 0)
                {
                    objName = objName.substring(0, j);
                }
                SXRSceneObject found = getOwnerObject().getSceneObjectByName(objName);
                if (found != null)
                {
                    partners = partners.replace(objName, "").trim();
                    if (partners.equals(""))
                    {
                        mMoleculeMap.remove(name);
                    }
                    else
                    {
                        mMoleculeMap.put(name, partners);
                    }
                    return found;
                }
            }
        }
        return null;
    }

    static public String getElementName(SXRSceneObject srcObj)
    {
        String name = srcObj.getName();
        String elemName = null;

        for (int i = 0; i < name.length(); ++i)
        {
            if ("0123456789".indexOf(name.charAt(i)) >= 0)
            {
                elemName = name.substring(0, i);
                return elemName;
            }
        }
        return null;
    }

    private void makeBond(SXRSceneObject sceneObj)
    {
        SXRSceneObject partner = getBondPoint(sceneObj);
        if (partner != null)
        {
            String name = partner.getName();
            if ((name != null) && name.startsWith(mElementName))
            {
                mTarget.setEnable(false);
                partner.getRenderData().setEnable(true);
                mTarget = null;
                if (mGoodSound != null)
                {
                    mGoodSound.play();
                }
                return;
            }
        }
        if (mBadSound != null)
        {
            mBadSound.play();
        }
        WrongAnswer = true;
        mTarget = null;
    }

    public void onTouch()
    {
        if (isEnabled() && (mTarget != null) && (mClosest != null))
        {
            makeBond(mClosest);
        }
    }

    public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
    public void onExit(SXRSceneObject sceneObj) { }
    public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
    public void onNoPick(SXRPicker picker) { }

    public void onPick(SXRPicker picker)
    {
        SXRSceneObject owner = getOwnerObject();
        if ((owner == null) || !isEnabled() || (mTarget == null))
        {
            return;
        }
        for (SXRPicker.SXRPickedObject picked : picker.getPicked())
        {
            SXRSceneObject hit = picked.hitObject;
            SXRRenderData rdata = hit.getRenderData();
            if ((rdata != null) && rdata.isEnabled())
            {
                mClosest = hit;
            }
        }
    }
}

