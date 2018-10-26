# sxrsdk-demos

## How to run these examples
* make sure you have [this repository](https://github.com/sxrsdk/sxrsdk-demos) and [the SXR SDK](https://github.com/sxrsdk/sxrsdk) in a sibling directory 

```
git clone https://github.com/sxrsdk/sxrsdk.git -b release_v5.0
git clone https://github.com/sxrsdk/sxrsdk-demos.git -b release_v5.0
```

There are two options:
- Clone the 5.0 branch: ``git clone -b release_v5.0 git@github.com:sxrsdk/sxrsdk-demos.git``
- Download a zip archive: open https://github.com/sxrsdk/sxrsdk-demos/tree/release_v5.0 and then select "Download ZIP"

* [download the Oculus_Mobile_SDK](https://developer3.oculus.com/downloads/mobile/1.0.4/Oculus_Mobile_SDK/) and put the SDK folder at the same directory level as your SXR directory.

```
#edit to use your PATH or make a env variable reference for wherever you unzipped the SDK
export OVR_MOBILE_SDK=~/mytools/ovr_sdk_mobile_1.0.4

```


* make sure you have gradle in your PATH (you can check that your PATH is setup correctly and verify your gradle version by running: `gradle -v` if you get a response, great! if not, [these instructions may help](https://developer.nvidia.com/codeworks-android)

* set up the [Oculus device signature](https://developer.oculus.com/osig/) for your device (it will look like oculussig_xxxxxxxx where xxxxxxxx is the id you get when you run `adb devices`) and copy it into an assets folder each project's `src/main/assets` directory: 

```
cp ~/Downloads/oculussig_xxxxxxxx ./sxr-cubemap/sxrcubemap/src/main/assets/oculussig_xxxxxxxx
```

* change to the example you wish to build and invoke the included gradle wrapper file, `./gradlew` 

```
cd sxr-sample
./gradlew assembleDebug
```

* you should now have an apk that you can push to an android device

```
adb install -r ./app/build/outputs/apk/app-debug.apk
```
NOTE: some of these examples use the application name instead of `app` to hold the apk build directory for instance, in sxr-cubemap, you will find the apk in `./sxr-cubemap/sxrcubemap/build/outputs/apk/app-debug.apk`

```
cd sxr-cubemap
./gradlew assembleDebug
adb install -r ./sxrcubemap/build/outputs/apk/app-debug.apk
```

* if you want to run without loading into a headset, enable Samsung VR Service developer mode: 
	- go to Settings > Applications > manage applications > Gear VR Service > Manage Storage
	- press the VR Service Version 6 times
	- if you get a message 'You are a developer' you should see a toggle to enable developer mode
	- if you get a message 'You are not a developer' you probably haven't installed a valid apk with your oculus signature - run the `adb install -r ./sxrcubemap/build/outputs/apk/app-debug.apk` command on at least one project, then the service should discover you
	 
	
### sxr-360photo
A minimal sample showing how to display an equirectangular (360) photo.

### sxr-360video
A minimal sample showing how to display an equirectangular (360) video using either Android's MediaPlayer class or the ExoPlayer class.

### sxr-3dcursor-simple
A simplified version of the sxr-3dcursor sample that shows how to use the 3DCursor plugin.

### sxr-3dcursor
Shows how to use the 3DCursor plugin.

### sxr-accessibility
Shows how to use SXR's accessibility classes.  For example: InvertedColors, TextToSpeech, and Zoom.

### sxr-bullet
Simple sample showing how to use SXR with the Physics plugin.

### sxr-camera2renderscript
Simple sample showing how to use the camera2 api along with renderscript for use with the passthrough camera.

### sxr-cardboard-audio
Simple example of Spatial Audio using GoogleVR's audio library (previously used cardboard's audio library).

### sxr-complexscene
A simple sample which can contain as many Stanford bunnies as we want to make it complex

### sxr-controls
A nice demo that shows input from both the gamepad and touchpad to control a character.

### sxr-cubemap
A simple example to show how to load in a cubemap and use it for the background as well as a reflection on an object.

### sxr-events
An example showing how to display Android Views inside VR and route events to those views.

### sxr-eyepicking
A simple picking example.

### sxr-gamepad
A minimal example showing how to receive input from a gamepad.

### sxr-immersivepedia
A larger sample that shows a concept of an immersive virtual museum.  Uses many features of SXR:  picking, TextViews, Video, input, etc.

### sxr-javascript
A minimal example showing how an application can be written with Javascript.

### sxr-keyboard
A sample that shows how to create a virtual keyboard, including voice input, and use it in a simple trivia game.

### sxr-lua
A minimal example showing how an application can be written with Lua.

### sxr-meshanimation
A simple sample that loads in an animated model and starts the animation.

### sxr-modelviewer2
A viewer that allows you to select and display models stored in /sdcard/SXRModelViewer2/.  You can look at the model from different angles, change lighting, look at it in wireframe, and toggle animations.  Uses the libGDX plugin for UI.

### sxr-multilight
A simple sample showing how to use multiple lights.

### sxr-outline
A sample showing how to use multiple render passes with the same geometry to show an outline.

### sxr-polyline
A sample showing how to draw lines.

### sxr-remote-scripting
The remote scripting sample enables the debug server and sets up a text object with the ipaddress of the phone so we know where to telnet into.

### sxr-renderableview
Inflates and displays some Android views onto a rotating cube.

### sxr-sceneobjects
Shows how create the various scene object types:  quad, cube, sphere, cylinder, cone, passthrough camera, text, video.  Tap the touchpad to cycle through the objects.

### sxr-shadows
A sample that shows a light source with shadowing.

### sxr-simplesample
A simple sample that creates a quad and applies a texture to it.  

### sxr-solarsystem
A sample that shows both hierarchy and animation.

### sxr-switch
A sample that shows how to use the SXRSwitch node.

### sxr-tutorial-lesson2
Shows initial scene setup and object creation.

### sxr-tutorial-lesson3
Shows event handling and picking.

### sxr-tutorial-lesson4
Shows creating and using components.  In this tutorial, a simple particle system is created.

### sxr-tutorial-lesson5
Shows how to integrate Sound and text.

### sxr-tutorial-lesson6
Shows loading and using model assets.

### sxr-video
A movie theater sample.  Plays back a video, has playback controls and has two theaters to choose from.

### sxr-vuforia
A simple augmented reality sample using the Vuforia computer vision library.  It looks for a marker and displays a teapot on top of it.  You can use either the stone or chips markers.  PDFs for the markers are in sxr-vuforia/app/src/main/.

### sxr-x3d-demo
Loads and displays an X3D model.


[Build tips](https://github.com/sxrsdk/sxrsdk-demos/wiki)
