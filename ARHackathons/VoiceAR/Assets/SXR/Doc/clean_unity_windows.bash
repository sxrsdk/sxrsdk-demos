#!/bin/bash
# clean_unity_windows.bash 
# run it at your cygwin work dir/bash
#
if [[ "$#" -ne 1 ]];  then
	echo "usage: $0 <path-to-unity-app-root>"
	exit 0
fi
UnityProjectPath=$1
#UnityProjectPath=$PWD  # use pwd instead in case "." does not work on your shell config
echo cleaning ${UnityProjectPath} ...
#
# *.so from gvrf
echo *.so from gvrf
rm -f ${UnityProjectPath}/Assets/Plugins/Android/armeabi-v7a/libassimp.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/armeabi-v7a/libgvrf.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/armeabi-v7a/libjnlua.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/armeabi-v7a/libgvrf-pure.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/armeabi-v7a/libgvrf-sensor.so

# *.so from gvrf
echo *.so from gvrf
rm -f ${UnityProjectPath}/Assets/Plugins/Android/libassimp.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/libgvrf.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/libjnlua.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/libgvrf-pure.so
rm -f ${UnityProjectPath}/Assets/Plugins/Android/libgvrf-sensor.so

# *.jar from gvrf
echo *.jar from gvrf
rm -f ${UnityProjectPath}/Assets/Plugins/Android/framework-classes.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/backend_pure-classes.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/gson-2.2.4.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/jline-android.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/jnlua-android.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/js.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/jsr223.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/pure-classes.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/joml-*
rm -f ${UnityProjectPath}/Assets/Plugins/Android/jav8.jar
rm -f ${UnityProjectPath}/Assets/Plugins/Android/libjav8.so

# *.meta (optionally can be deleted but not required)
#rm -f ${UnityProjectPath}/Assets/Plugins/Android/*.meta

# *.cs and misc from gvrf
echo *.cs and misc from gvrf
rm -rf ${UnityProjectPath}/Assets/GEARVR/Editor
rm -rf ${UnityProjectPath}/Assets/GEARVR/Prefabs
rm -rf ${UnityProjectPath}/Assets/GEARVR/Resources
rm -rf ${UnityProjectPath}/Assets/GEARVR/Scripts

echo done
