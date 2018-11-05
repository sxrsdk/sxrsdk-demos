importPackage(com.samsungxr);
importPackage(com.samsungxr.nodes);

var sphere = new SXRSphereNode(sxr, false);
var mesh = sphere.getRenderData().getMesh();
var mediaPlayer = com.samsungxr.utility.ImageUtils.createMediaPlayer("https://github.com/sxrsdk/sxrsdk-demos/raw/master/sxr-360video/app/src/main/assets/video.mp4");
var video = new SXRVideoNode(sxr, mesh, mediaPlayer, SXRVideoNode.SXRVideoType.MONO );
video.setName("video");
var scene = sxr.getMainScene();
scene.addNode(video);

mediaPlayer.start();
