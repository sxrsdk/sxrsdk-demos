// C# example:
using UnityEngine;
using UnityEditor;
using UnityEditor.Callbacks;
using System.IO;

namespace Sxr {

    public class PostProcessChecker {
        [PostProcessBuildAttribute(0)]
        public static void OnPostprocessBuild(BuildTarget target, string pathToBuiltProject) {

            if (!File.Exists(Application.dataPath + "/Plugins/Android/AndroidManifest.xml"))
                Debug.LogError("Missing Manifest.xml, the build will not work properlly, please reimport the plugin");
            pathToBuiltProject = null;
        }
    }
}
