importPackage(com.samsungxr)
importPackage(com.samsungxr.nodes)
importPackage(com.samsungxr.script)
importPackage(com.samsungxr.animation)

function onInit(sxr) {
  var mainScene = sxr.getMainScene();

  // 3D Boat Scene
  var boat = sxr.getAssetLoader().loadModel("RowBoatFBX/RowBoatAnimated.fbx", mainScene);
  boat.setName("boat");
  boat.getTransform().setScale(.5, .5, .5);
  boat.getTransform().setRotationByAxis(20, 0, 1, 0);
  boat.getTransform().setPosition(20, -20, -40);

  // Text
  var textView = new SXRTextViewNode(sxr);
  textView.setText("SXR scripting in Javascript");
  textView.setRefreshFrequency(SXRTextViewNode.IntervalFrequency.REALTIME);
  textView.setName("text");
  textView.setTextSize(6);
  textView.getTransform().setPosition(0, 0, -100);
  textView.getTransform().setScale(50, 50, 50);

  var textSensor = new SXRSensor(sxr);

  textView.attachComponent(textSensor);
  var script = new SXRScriptBehavior(sxr, "text.js");
  textView.attachComponent(script);
  mainScene.addNode(textView);

  // Animation
  var animation = boat.getComponent(SXRAnimator.getComponentType());
  if (animation)
  {
      animation.setRepeatMode(1);
      animation.setRepeatCount(-1);
      animation.start(animation.getSXRContext().getAnimationEngine());
  }

  mainScene.setFrustumCulling(false);
}
