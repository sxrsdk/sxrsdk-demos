using UnityEditor;
using UnityEngine;

//Jenkins uses. namespace Sxr {

    public static class PluginBuilder {

        public static void PackageSXRUnityPlugin() {

            string[] assetPaths = new string[] {
            "Assets/SXR/config.xml",
            "Assets/SXR/Doc",
            "Assets/SXR/Editor",
            "Assets/SXR/Prefabs",
            "Assets/SXR/Resources",
            "Assets/SXR/Scripts",
            "Assets/SXR/Util",
            "Assets/Plugins/Android",
            "Assets/StreamingAssets/Xml"
        };

            string packagePath = "SXRPlugin.unitypackage";
            ExportPackageOptions options = ExportPackageOptions.Recurse;
            AssetDatabase.ExportPackage(assetPaths, packagePath, options);
        }
    }
//}
