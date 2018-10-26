importPackage(org.gearvrf);
importPackage(org.gearvrf.nodes);

var url = new java.net.URL("https://raw.githubusercontent.com/gearvrf/GearVRf-Demos/master/gvr-360photo/app/src/main/res/raw/photosphere.jpg");
var resource = new GVRAndroidResource(gvrf, url);
var texture = gvrf.getAssetLoader().loadTexture(resource);
var photosphere = new GVRSphereNode(gvrf, false, texture);
photosphere.getTransform().setScale(20, 20, 20);
photosphere.setName("photosphere");
var scene = gvrf.getMainScene();
scene.addNode(photosphere);

