importPackage(com.samsungxr);
importPackage(com.samsungxr.nodes);

var url = new java.net.URL("https://raw.githubusercontent.com/sxrsdk/sxrsdk-demos/master/sxr-360photo/app/src/main/res/raw/photosphere.jpg");
var resource = new SXRAndroidResource(sxr, url);
var texture = sxr.getAssetLoader().loadTexture(resource);
var photosphere = new SXRSphereNode(sxr, false, texture);
photosphere.getTransform().setScale(20, 20, 20);
photosphere.setName("photosphere");
var scene = sxr.getMainScene();
scene.addNode(photosphere);

