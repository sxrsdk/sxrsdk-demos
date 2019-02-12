# SXR-ARPET
A pet experience in Augmented Reality (AR).

## Main features
* Plane detection and point cloud to show the AR detected points to compose the plane.
* Share AR experience using Cloud Anchors and connect several devices using Bluetooth.
* Physics simulation working with AR.
* 3D models, animations and Android views in AR environment.
* Capture and share screen images.

## Configure Cloud Anchor API Key
This application uses [Google Cloud Anchor service](https://developers.google.com/ar/develop/java/cloud-anchors/quickstart-android) to share the AR experience between several devices. For this, we need to create and export a Cloud Anchor API key, which can be obtained in [Google Cloud Platform](https://console.cloud.google.com/apis/library/arcorecloudanchor.googleapis.com).

Once you have the key, configure it in your global `gradle.properties` file using the variable name *cloudAnchorApiKey* (like this: *cloudAnchorApiKey=XXXX, where XXXX is your API key*) and build the application.

The sharing experience uses Bluetooth to make the communication between the devices. Don't forget to turn it on before starting the sharing mode.

After that, you are able to start the sharing anchor mode. Enjoy it and have fun!