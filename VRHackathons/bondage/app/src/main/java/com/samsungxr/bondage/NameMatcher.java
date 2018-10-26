package com.samsungxr.bondage;

import com.samsungxr.SXRNode;

public class NameMatcher implements SXRNode.SceneVisitor
{
    private String mElementName;
    public SXRNode Match;

    public NameMatcher(String elementName)
    {
        Match = null;
        mElementName = elementName;
    }

    public boolean visit(SXRNode srcObj)
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
