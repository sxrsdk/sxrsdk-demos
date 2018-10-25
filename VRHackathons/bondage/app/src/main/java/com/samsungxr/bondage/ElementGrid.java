package com.samsungxr.bondage;


import com.samsungxr.SXRBehavior;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPhongShader;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
import com.samsungxr.utility.Log;

public class ElementGrid extends SXRBehavior implements SXRSceneObject.SceneVisitor
{
    static private long TYPE_ELEMENT_GRID = newComponentType(ElementGrid.class);
    static private final float ELEMENT_SCALE = 0.5f;

    public ElementGrid(SXRContext ctx)
    {
        super(ctx);
        mType = TYPE_ELEMENT_GRID;
        mGridPositions = new float[][]
        {
                { -2.0f, 2.0f, 0.0f },
                { 0.0f, 2.0f, 0.0f },
                { 2.0f, 2.0f, 0.0f },
                { -2.0f, 0.0f, 0.0f },
                { 0.0f, 0.0f, 0.0f },
                { 2.0f, 0.0f, 0.0f },
                { -2.0f, -2.0f, 0.0f },
                { 0.0f, -2.0f, 0.0f },
                { 2.0f, -2.0f, 0.0f },
        };
    }

    public void onAttach(SXRSceneObject owner)
    {
        if (owner.getChildrenCount() == 0)
        {
            for (float[] pos : mGridPositions)
            {
                SXRSceneObject element = new SXRSceneObject(owner.getSXRContext());
                element.getTransform().setPosition(pos[0], pos[1], pos[2]);
                owner.addChildObject(element);
            }
        }
    }

    private SXRSceneObject findEmptyGridSlot()
    {
        for (SXRSceneObject child : owner.children())
        {
            if (child.getChildrenCount() == 0)
            {
                return child;
            }
        }
        return null;
    }

    static public long getComponentType() { return TYPE_ELEMENT_GRID; }

    private float[][] mGridPositions;
    private int mGridIndex = 0;

    public void makeGrid(SXRSceneObject srcRoot)
    {
        srcRoot.forAllDescendants(this);
    }

    public void addToGrid(SXRSceneObject newElem)
    {
        SXRSceneObject gridParent = findEmptyGridSlot();
        if (gridParent != null)
        {
            SXRTransform trans = newElem.getTransform();
            trans.setPosition(0, 0, 0);
            trans.setScale(ELEMENT_SCALE, ELEMENT_SCALE, ELEMENT_SCALE);
            gridParent.addChildObject(newElem);
        }
    }

    public boolean visit(SXRSceneObject srcObj)
    {
        SXRRenderData srcRender = srcObj.getRenderData();
        SXRSceneObject owner = getOwnerObject();
        SXRSceneObject dstRoot = findEmptyGridSlot();

        if ((srcRender != null) && (dstRoot != null))
        {
            SXRContext ctx = srcObj.getSXRContext();
            SXRMaterial srcMtl = srcRender.getMaterial();
            SXRMesh srcMesh = srcRender.getMesh();

            if ((srcMtl == null) || (srcMesh == null))
            {
                return true;
            }
            SXRSceneObject dstObj = new SXRSphereSceneObject(ctx);
            SXRRenderData dstRender = new SXRRenderData(ctx);
            SXRTransform dstTrans = dstObj.getTransform();
            SXRSphereCollider collider = new SXRSphereCollider(ctx);
            SXRMaterial dstMtl = new SXRMaterial(ctx);
            String name = srcObj.getName();
            SXRTexture tex = srcMtl.getTexture("diffuseTexture");
            float[]     temp;

            if (tex != null)
            {
                dstMtl.setTexture("diffuseTexture", tex);
            }
            dstMtl.setSpecularColor(1.0f, 1.0f, 1.0f, 1.0f);
            dstMtl.setSpecularExponent(10.0f);
            srcMtl.setSpecularExponent(10.0f);
            temp = srcMtl.getAmbientColor();
            dstMtl.setAmbientColor(temp[0], temp[1], temp[2], temp[3]);
            temp = srcMtl.getDiffuseColor();
            dstMtl.setDiffuseColor(temp[0], temp[1], temp[2], temp[3]);
            temp = srcMtl.getSpecularColor();
            dstMtl.setSpecularColor(temp[0], temp[1], temp[2], temp[3]);
            dstObj.setName(name);
            dstTrans.setScale(ELEMENT_SCALE, ELEMENT_SCALE, ELEMENT_SCALE);

            dstRender.setMaterial(dstMtl);
            dstRender.setMesh(dstObj.getRenderData().getMesh());
            dstRender.setShaderTemplate(SXRPhongShader.class);
            dstObj.detachComponent(SXRRenderData.getComponentType());
            dstObj.attachComponent(dstRender);
            dstObj.attachComponent(collider);
            collider = new SXRSphereCollider(ctx);
            srcObj.attachComponent(collider);
            dstRoot.addChildObject(dstObj);
            owner.addChildObject(dstRoot);
            srcRender.setEnable(false);
        }
        return true;
    }
}