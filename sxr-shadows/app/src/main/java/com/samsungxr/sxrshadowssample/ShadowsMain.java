package com.samsungxr.sxrshadowssample;

import java.io.IOException;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;

import com.samsungxr.SXRMaterial;
import com.samsungxr.shaders.SXRPhongShader;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRSpotLight;

import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.nodes.SXRSphereNode;
import android.graphics.Color;
import android.view.MotionEvent;

public class ShadowsMain extends SXRMain {

    private SXRContext mSXRContext = null;
    private SXRNode cubeObject = null;
    private SXRNode rotateObject = null;
    private SXRNode lightObject = null;

    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {
        mSXRContext = sxrContext;

        SXRScene scene = mSXRContext.getMainScene();
        SXRCameraRig mainCameraRig = scene.getMainCameraRig();

        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.setFarClippingDistance(100.0f);
        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 8.0f);

        SXRNode groundScene = createBackdrop(sxrContext);
        groundScene.getTransform().setRotationByAxis(-80.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addNode(groundScene);

        addSphere(scene, 1.0f, 0, 1.0f, -1.0f);
        addSphere(scene, 2, -4, 2.0f, -2.0f);
        addCube(scene, 2, 6f, 2, -3.0f);
        addStormtrooper(scene, 0, 2.6f, -2.0f);
        lightObject = createSpotLight(sxrContext);
        scene.addNode(lightObject);
    }

    private SXRNode createBackdrop(SXRContext context) throws IOException
    {
        SXRTexture tex = context.getAssetLoader().loadTexture(new SXRAndroidResource(mSXRContext, "floor.jpg"));
        SXRNode backdrop = new SXRNode(context, 100.0f, 100.0f, tex);
        SXRRenderData rdata = backdrop.getRenderData();
        SXRMaterial material = new SXRMaterial(context,new SXRShaderId(SXRPhongShader.class));

        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
        backdrop.setName("Backdrop");
        rdata.setMaterial(material);
        return backdrop;
    }

    private SXRNode createDirectLight(SXRContext context)
    {
        SXRNode lightNode = new SXRNode(context);
        SXRDirectLight light = new SXRDirectLight(context);

        light.setCastShadow(true);
        lightNode.attachLight(light);
        light.setShadowRange(1.0f, 150.0f);
        lightNode.getTransform().setRotationByAxis(-70, 1, 0, 0);
        light.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1);
        light.setDiffuseIntensity(1, 1, 1, 1);
        light.setSpecularIntensity(1, 1, 1, 1);
        light.setShadowRange(1f, 150.0f);
        lightNode.setName("DirectLight");
        return lightNode;
    }

    private SXRNode createSpotLight(SXRContext context)
    {
        SXRNode lightNode = new SXRNode(context);
        SXRSpotLight light = new SXRSpotLight(context);

        light.setCastShadow(true);
        lightNode.attachLight(light);
        lightNode.getTransform().setRotationByAxis(-35, 1, 0, 0);
        lightNode.getTransform().setPosition(-4, 7, 10);
        light.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1);
        light.setDiffuseIntensity(1, 1, 1, 1);
        light.setSpecularIntensity(1, 1, 1, 1);
        light.setInnerConeAngle(40);
        light.setOuterConeAngle(55);
        light.setShadowRange(1f, 100.0f);
        lightNode.setName("SpotLight");

        return lightNode;
    }

    private float theta = 0.0f;

    @Override
    public void onStep()
    {
       if (rotateObject == null)
            return;
        SXRTransform trans = rotateObject.getTransform();
        float xrot = trans.getRotationPitch();
        float yrot = trans.getRotationYaw();
        float xpos = trans.getPositionX();
        float ypos = trans.getPositionY();
        float zpos = trans.getPositionZ();

        if (yrot < -45.0f)
        {
            yrot = 45.0f;
            if (xrot <= -80.0f)
            {
                xrot = -20.0f;
            }
            else
            {
                xrot -= 2.0f;
            }
        }
        else
        {
            --yrot;
        }
        trans.reset();
        trans.rotateByAxis(xrot, 1, 0, 0);
        trans.rotateByAxis(yrot, 0, 1, 0);
        trans.setPosition(xpos, ypos, zpos);

    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            if (rotateObject == null)
            {
                rotateObject = lightObject;
            }
            else
            {
                rotateObject = null;
            }
            break;

            default:
            break;
        }
    }

    private SXRMaterial createCustomMaterial(SXRContext context, String textureFile) throws IOException
    {
        SXRMaterial litMaterial = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);

        litMaterial.setVec4("diffuse_color", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("ambient_color", 0.5f, 0.5f, 0.5f, 0.0f);
        litMaterial.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        litMaterial.setFloat("specular_exponent", 10.0f);

        SXRTexture texture = context.getAssetLoader().loadTexture(new SXRAndroidResource(context, textureFile));
        litMaterial.setTexture("diffuseTexture", texture);
        return litMaterial;
    }

    private void addCube(SXRScene scene, float size, float x, float y, float z) throws IOException
    {
        cubeObject = new SXRCubeNode(mSXRContext, true, createCustomMaterial(mSXRContext, "cube.jpg"));
        cubeObject.getTransform().setPosition(x, y, z);
        cubeObject.getTransform().setScale(size, size, size);
        cubeObject.setName("cube");
        scene.addNode(cubeObject);
    }

    private void addSphere(SXRScene scene, float radius, float x, float y, float z) throws IOException
    {
        SXRNode sphereObject  = new SXRSphereNode(mSXRContext, true, createCustomMaterial(mSXRContext, "sphere.jpg"));

        sphereObject.setName("sphere");
        sphereObject.getTransform().setPosition(x, y, z);
        sphereObject.getTransform().setScale(radius, radius, radius);
        scene.addNode(sphereObject);
    }

    private SXRNode addStormtrooper(SXRScene scene, float x, float y, float z) throws IOException
    {
        SXRNode model = mSXRContext.getAssetLoader().loadModel("storm.obj", scene);
        model.getTransform().setPosition(x, y, z);
        model.getTransform().setScale(1.5f, 1.5f, 1.5f);
        model.getTransform().setRotationByAxis((float) -90, 0, 1, 0);
        return model;
    }

}
