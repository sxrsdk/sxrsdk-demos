importPackage(org.gearvrf);
importPackage(org.gearvrf.nodes);

var sphere = new GVRSphereNode(gvrf, false);
var mesh = sphere.getRenderData().getMesh();
var mediaPlayer = org.gearvrf.utility.ImageUtils.createMediaPlayer("https://github.com/gearvrf/GearVRf-Demos/raw/master/gvr-360video/app/src/main/assets/video.mp4");
var video = new GVRVideoNode(gvrf, mesh, mediaPlayer, GVRVideoNode.GVRVideoType.MONO );
video.setName("video");
var scene = gvrf.getMainScene();
scene.addNode(video);

mediaPlayer.start();
