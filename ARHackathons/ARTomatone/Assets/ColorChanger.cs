using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using MPS.XR;

public class ColorChanger : MonoBehaviour, ISXRControllerTriggerHandler
{
    public bool triggerClicked;
    public bool triggerDown;
    public bool triggerUp;

    void ISXRControllerTriggerHandler.OnTriggerClicked()
    {
        triggerClicked = true;
        Debug.Log("trigger clicked");
        //        throw new System.NotImplementedException();
    }

    void ISXRControllerTriggerHandler.OnTriggerDown()
    {
        triggerDown = true;
        Debug.Log("trigger down");
    }

    void ISXRControllerTriggerHandler.OnTriggerUp()
    {
        triggerUp = true;
        Debug.Log("trigger up");
        //        throw new System.NotImplementedException();
        Vector3 r = Random.insideUnitSphere;
        this.GetComponent<MeshRenderer>().sharedMaterial.color = new Color(Mathf.Abs(r.x), Mathf.Abs(r.y), Mathf.Abs(r.z));
    }

    // Start is called before the first frame update
    void Start()
    {
        SXRInputModule.Register(this, gameObject);
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
