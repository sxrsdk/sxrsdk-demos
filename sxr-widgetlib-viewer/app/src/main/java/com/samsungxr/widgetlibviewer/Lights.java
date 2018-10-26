package com.samsungxr.widgetlibviewer;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSpotLight;
import org.joml.Vector4f;

import java.util.ArrayList;

public class Lights {
    private ArrayList<Vector4f> ambient = new ArrayList<Vector4f>();
    private ArrayList<Vector4f> diffuse = new ArrayList<Vector4f>();
    private ArrayList<Vector4f> specular = new ArrayList<Vector4f>();

    private Vector4f defaultAmbient = new Vector4f(1, 1, 1, 0.5f);
    private Vector4f defaultDiffuse = new Vector4f(1, 1, 1, 0.5f);
    private Vector4f defaultSpecular = new Vector4f(1, 1, 1, 0.5f);


    private SXRSpotLight bulb;
    private SXRNode bulbScene;

    public void addAmbient(float r, float g, float b, float a) {
        ambient.add(new Vector4f(r, g, b, a));
    }

    public void addDiffuse(float r, float g, float b, float a) {
        diffuse.add(new Vector4f(r, g, b, a));
    }

    public void addSpecular(float r, float g, float b, float a) {
        specular.add(new Vector4f(r, g, b, a));
    }

    public ArrayList<Vector4f> getAmbient() {
        return ambient;
    }

    public ArrayList<Vector4f> getDiffuse() {
        return diffuse;
    }

    public ArrayList<Vector4f> getSpecular() {
        return specular;
    }

    public void createLight(SXRContext context) {
        bulbScene = new SXRNode(context);
        bulb = new SXRSpotLight(context);

        bulbScene.attachLight(bulb);

        setDefaultLight();

        bulb.setInnerConeAngle(100);
        bulb.setOuterConeAngle(100);
    }

    public SXRSpotLight getLight() {
        return bulb;
    }

    public SXRNode getLightScene() {
        return bulbScene;
    }

    public void setDefaultLight() {
        bulb.setAmbientIntensity(defaultAmbient.x, defaultAmbient.y, defaultAmbient.z, defaultAmbient.w);
        bulb.setDiffuseIntensity(defaultDiffuse.x, defaultDiffuse.y, defaultDiffuse.z, defaultDiffuse.w);
        bulb.setSpecularIntensity(defaultSpecular.x, defaultSpecular.y, defaultSpecular.z, defaultSpecular.w);
    }

    public void setSelected(int index) {
        bulb.setAmbientIntensity(ambient.get(index).x, ambient.get(index).y, ambient.get(index).z, ambient.get(index).w);
        bulb.setDiffuseIntensity(diffuse.get(index).x, diffuse.get(index).y, diffuse.get(index).z, diffuse.get(index).w);
        bulb.setSpecularIntensity(specular.get(index).x, specular.get(index).y, specular.get(index).z, specular.get(index).w);
    }

    public void setAmbient(int index) {
        bulb.setAmbientIntensity(ambient.get(index).x, ambient.get(index).y, ambient.get(index).z, ambient.get(index).w);
    }

    public void setDiffuse(int index) {
        bulb.setDiffuseIntensity(diffuse.get(index).x, diffuse.get(index).y, diffuse.get(index).z, diffuse.get(index).w);
    }

    public void setSpecular(int index) {
        bulb.setSpecularIntensity(specular.get(index).x, specular.get(index).y, specular.get(index).z, specular.get(index).w);
    }

    // START Lights
    public void loadLights(SXRContext context) {
        createLight(context);

        //Add white light source
        addAmbient(0.5f, 0.5f, 0.5f, 0.5f);
        addDiffuse(0.5f, 0.5f, 0.5f, 0.5f);
        addSpecular(0.5f, 0.5f, 0.5f, 0.5f);

        // Add Ambient
        addAmbient(0.3f, 0.0f, 0.0f, 0.5f);

        // Add Diffuse
        addDiffuse(0.3f, 0.0f, 0.0f, 0.5f);

        // Add Specular
        addSpecular(0.3f, 0.0f, 0.0f, 0.5f);

        // Add Ambient
        addAmbient(0.0f, 0.0f, 0.5f, 0.5f);

        // Add Diffuse
        addDiffuse(0.0f, 0.0f, 0.5f, 0.5f);

        // Add Specular
        addSpecular(0.0f, 0.0f, 0.5f, 0.5f);

        // Add Ambient
        addAmbient(0.0f, 0.3f, 0.0f, 0.5f);

        // Add Diffuse
        addDiffuse(0.0f, 0.3f, 0.0f, 0.5f);

        // Add Specular
        addSpecular(0.0f, 0.3f, 0.0f, 0.5f);
    }

}
