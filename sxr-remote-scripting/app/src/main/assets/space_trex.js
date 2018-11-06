importPackage(com.samsungxr);
importPackage(com.samsungxr.nodes);
importPackage(com.samsungxr.animation);

// get a handle to the scene
var scene = sxr.getMainScene();

// space background
var space_url = new java.net.URL("https://github.com/sxrsdk/sxrsdk-demos/raw/master/sxr-remote-scripting/app/src/main/assets/space.jpg");
var space_resource = new SXRAndroidResource(sxr, space_url);
var space_texture = sxr.getAssetLoader().loadTexture(space_resource);
var space_photosphere = new SXRSphereNode(sxr, false, space_texture);
space_photosphere.getTransform().setScale(20, 20, 20);
space_photosphere.setName("space_photosphere");
scene.addNode(space_photosphere);

// space platform
var platform_url = new java.net.URL("https://github.com/sxrsdk/sxrsdk-demos/raw/master/sxr-remote-scripting/app/src/main/assets/platform.fbx");
var platform = sxr.getAssetLoader().loadModel(platform_url, scene);
platform.getTransform().setPosition(0, -2, -10);
platform.setName("platform");

// space trex
var node = new SXRNode(sxr);
var trex_url = new java.net.URL("https://github.com/sxrsdk/sxrsdk-demos/raw/master/sxr-meshanimation/app/src/main/assets/TRex_NoGround.fbx");
var trex = sxr.getAssetLoader().loadModel(trex_url);
trex.setName("trex");

node.addChildObject(trex);
scene.addNode(node);

// animate trex
var animator = trex.getComponent(SXRAnimator.getComponentType());
animator.setRepeatMode(1);
animator.setRepeatCount(-1);
node.getTransform().setPosition(0.0, -2.0, -10.0);
trex.getTransform().setRotationByAxis(90.0, 1.0, 0.0, 0.0);
animator.start();

