using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Reflection;

namespace Sxr {

    /// <summary>
    /// Common SXR extention methods. this class must be static.
    /// </summary>
    public static class SXRExtension {
        /// <summary>
        /// Reset transform
        /// </summary>
        public static void ResetTransformation(this Transform trans) {
            trans.position = Vector3.zero;
            trans.localRotation = Quaternion.identity;
            trans.localScale = new Vector3(1, 1, 1);
        }

        /// <summary>
        /// Same as transform.FindChild but this returns a GameObject reference, not a transform.
        /// </summary>
        public static string FullPath(this GameObject go) {
            if (go == null) {
                Debug.LogException(new ArgumentNullException("self"));
                return string.Empty;
            }

            Stack<string> hierarchyObjectNames = new Stack<string>();

            Transform current = go.transform;
            while (current != null) {
                hierarchyObjectNames.Push(current.gameObject.name);
                current = current.parent;
            }

            StringBuilder sb = new StringBuilder();
            while (hierarchyObjectNames.Count > 0) {
                sb.Append(hierarchyObjectNames.Pop());
                sb.Append('.');
            }

            return sb.ToString();
        }

        /// <summary>
        /// returns a child GameObject reference under the given name of GameObject.
        /// </summary>
        public static GameObject GetChild(this GameObject go, string childName) {
            if (string.IsNullOrEmpty(childName))
                return null;

            var result = go.transform.Find(childName);

            if (result == null)
                return null;

            return result.gameObject;
        }

        /// <summary>
        /// returns a children GameObject list.
        /// </summary>
        public static List<GameObject> GetChildren(this GameObject go) {
            List<GameObject> children = new List<GameObject>();

            if (go == null) {
                return children;
            }

            int childCount = go.transform.childCount;

            for (int i = 0; i <= childCount - 1; i++) {
                children.Add(go.transform.GetChild(i).gameObject);
            }

            return children;
        }

        /// <summary>
        /// return all of hierarchy children GameObject list based upon the given list.
        /// </summary>
        public static List<GameObject> GetChildren(this GameObject go, List<GameObject> list) {
            if (go == null) {
                return list;
            }

            foreach (Transform child in go.transform) {
                list.Add(child.gameObject);
                GetChildren(child.gameObject, list);
            }
            return list;
        }

        /// <summary>
        /// return all of hierarchy children GameObject list.
        /// </summary>
        public static List<GameObject> GetAllChildren(this GameObject go) {
            List<GameObject> children = new List<GameObject>();

            if (go == null) {
                return children;
            }

            return GetChildren(go, children);
        }

        /// <summary>
        /// Destroy and detach children GameObject.
        /// </summary>
        public static void DestroyChild(this Transform transform) {
            if (transform != null) {
                foreach (Transform child in transform) {
                    UnityEngine.Object.Destroy(child.gameObject);
                }
                transform.DetachChildren();
            }
        }

        /// <summary>
        /// Destory all of children GameObject.
        /// </summary>
        public static void DestroyAllChildren(this GameObject go) {
            if (go != null) {
                int count = go.transform.childCount;
                for (int i = 0; i < count; i++) {
                    Transform child = go.transform.GetChild(i);
                    do {
                        if (child.childCount > 0) {
                            DestroyChild(child);
                        }
                        DestroyAllChildren(child.gameObject);
                        child = go.transform.GetChild(i);
                    } while (child.childCount > 0);

                    UnityEngine.Object.Destroy(child.gameObject);
                }
                // detaching children
                go.transform.DetachChildren();
            }
        }



        /// <summary>
        /// Dictionary extention for casting of differnt types
        /// </summary>
        public static T Get<T>(this Dictionary<string, object> instance, string name) {
            return (T)instance[name];
        }

        /// <summary>
        /// Add and Copy component to the given GameObject
        /// </summary>
        public static T CopyComponent<T>(this GameObject go, T toAdd) where T : Component {
            return go.AddComponent<T>().GetCopyOf(toAdd) as T;
        }

        /// <summary>
        /// Copy all of component's properties to the given component
        /// </summary>
        public static T GetCopyOf<T>(this Component comp, T other) where T : Component {
            Type type = comp.GetType();
            if (type != other.GetType()) {
                return null; // type mis-match
            }

            BindingFlags flags = BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Default | BindingFlags.DeclaredOnly;
            PropertyInfo[] pinfos = type.GetProperties(flags);
            foreach (var pinfo in pinfos) {
                if (pinfo.CanWrite) {
                    try {
                        pinfo.SetValue(comp, pinfo.GetValue(other, null), null);
                    } catch { } // In case of NotImplementedException being thrown. For some reason specifying that exception didn't seem to catch it, so I didn't catch anything specific.
                }
            }

            FieldInfo[] finfos = type.GetFields(flags);
            foreach (FieldInfo finfo in finfos) {
                // Transform should be attached to target game object or component
                if (finfo.FieldType == typeof(Transform)) {
                    finfo.SetValue(comp, comp.transform);
                } else if (finfo.FieldType == typeof(GameObject)) {
                    // TODO: Check this valid or not (e.g. it should copies all of children components from the given game objects)
                    finfo.SetValue(comp, finfo.GetValue(other));

                    if (finfo.FieldType.IsSerializable == false) {
                        //Debug.Log(finfo.Name + " (Type: " + finfo.FieldType + ") is not marked as serializable. Continue loop.");
                        continue;
                    }

                    if (IsEnumerableType(finfo.FieldType) == true || IsCollectionType(finfo.FieldType) == true) {
                        Type elementType = GetElementType(finfo.FieldType);
                        //Debug.Log(finfo.Name + " -> " + elementType);

                        if (elementType.IsSerializable == false) { continue; }
                    }
                } else {
                    finfo.SetValue(comp, finfo.GetValue(other));
                }
            }

            return comp as T;
        }

        /// <summary>
        /// Get element type
        /// </summary>
        internal static Type GetElementType(Type seqType) {
            Type ienum = FindIEnumerable(seqType);
            if (ienum == null) {
                return seqType;
            }

            return ienum.GetGenericArguments()[0];
        }

        /// <summary>
        /// Find IEnumerable
        /// </summary>
        private static Type FindIEnumerable(Type seqType) {
            if (seqType == null || seqType == typeof(string)) {
                return null;
            }

            if (seqType.IsArray) {
                return typeof(IEnumerable<>).MakeGenericType(seqType.GetElementType());
            }

            if (seqType.IsGenericType) {
                foreach (Type arg in seqType.GetGenericArguments()) {
                    Type ienum = typeof(IEnumerable<>).MakeGenericType(arg);
                    if (ienum.IsAssignableFrom(seqType)) {
                        return ienum;
                    }
                }
            }

            Type[] ifaces = seqType.GetInterfaces();
            if (ifaces != null && ifaces.Length > 0) {
                foreach (Type iface in ifaces) {
                    Type ienum = FindIEnumerable(iface);
                    if (ienum != null) { return ienum; }
                }
            }

            if (seqType.BaseType != null && seqType.BaseType != typeof(object)) {
                return FindIEnumerable(seqType.BaseType);
            }

            return null;
        }

        /// <summary>
        /// Is a type of Enumerable?
        /// </summary>
        public static bool IsEnumerableType(Type type) {
            return (type.GetInterface("IEnumerable") != null);
        }

        /// <summary>
        /// Is a type of collection?
        /// </summary>
        public static bool IsCollectionType(Type type) {
            return (type.GetInterface("ICollection") != null);
        }
    }
}
