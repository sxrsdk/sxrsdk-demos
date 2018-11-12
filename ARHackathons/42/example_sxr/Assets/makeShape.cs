using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class makeShape : MonoBehaviour {

    private void OnTriggerEnter2D(Collider2D collision)
    {
        Debug.Log("Triggered");
    }
    private void OnTriggerStay2D(Collider2D collision)
    {
        Debug.Log("Triggered S");
    }
    private void OnTriggerExit2D(Collider2D collision)
    {
        Debug.Log("Triggered E");
        print("YOU DIED");
        Application.LoadLevel(Application.loadedLevel);
    }
}
