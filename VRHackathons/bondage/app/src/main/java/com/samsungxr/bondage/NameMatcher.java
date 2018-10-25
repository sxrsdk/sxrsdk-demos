package com.samsungxr.bondage;

import com.samsungxr.SXRSceneObject;

public class NameMatcher implements SXRSceneObject.SceneVisitor
{
    private String mElementName;
    public SXRSceneObject Match;

    public NameMatcher(String elementName)
    {
        Match = null;
        mElementName = elementName;
    }

    public boolean visit(SXRSceneObject srcObj)
    {
        String name = srcObj.getName();

        if (name.endsWith(".obj"))
        {
            return true;
        }
        if (name.startsWith(mElementName))
        {
            Match = srcObj;
            return false;
        }
        return true;
    }
}
