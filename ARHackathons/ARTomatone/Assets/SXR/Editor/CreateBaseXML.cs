using UnityEngine;
using UnityEditor;
using System.Collections;
using System.Xml;

namespace Sxr {

    public class CreateBaseXML : Editor {

        public static void BaseXML() {

            XmlDocument doc = new XmlDocument();

            XmlDeclaration xmlDeclaration = doc.CreateXmlDeclaration("1.0", "UTF-8", null);
            XmlElement root = doc.DocumentElement;
            doc.InsertBefore(xmlDeclaration, root);


            XmlElement parameters = doc.CreateElement(string.Empty, "parameters", string.Empty);
            doc.AppendChild(parameters);

            XmlElement paramEyeDistance = doc.CreateElement(string.Empty, "EyeDistance", string.Empty);
            XmlText text = doc.CreateTextNode("0.5");
            paramEyeDistance.AppendChild(text);
            parameters.AppendChild(paramEyeDistance);

            XmlElement paramCopyParentCamera = doc.CreateElement(string.Empty, "CopyParentCamera", string.Empty);
            text = doc.CreateTextNode("false");
            paramCopyParentCamera.AppendChild(text);
            parameters.AppendChild(paramCopyParentCamera);

            XmlElement paramDistortionType = doc.CreateElement(string.Empty, "DistortionType", string.Empty);
            text = doc.CreateTextNode("Grid");
            paramDistortionType.AppendChild(text);
            parameters.AppendChild(paramDistortionType);

            doc.Save(Application.dataPath + "/SXR/config.xml");
            AssetDatabase.Refresh();

        }
    }
}
