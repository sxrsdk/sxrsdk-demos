importPackage(com.samsungxr);

var scene = sxr.getMainScene();

var trex_url = new java.net.URL("https://github.com/sxrsdk/sxrsdk-demos/raw/master/sxr-meshanimation/app/src/main/assets/TRex_NoGround.fbx");
var trex = sxr.getAssetLoader().loadModel(trex_url, scene);

trex.getTransform().setPosition(0.0, -10.0, -10.0);
trex.getTransform().setRotationByAxis(90.0, 1.0, 0.0, 0.0);
trex.getTransform().setRotationByAxis(40.0, 0.0, 1.0, 0.0);
trex.getTransform().setScale(1.5, 1.5, 1.5);
trex.setName("trex");
var engine = sxr.getAnimationEngine();
var animation = trex.getAnimations().get(0);
animation.setRepeatMode(1).setRepeatCount(-1);
animation.start(engine);

