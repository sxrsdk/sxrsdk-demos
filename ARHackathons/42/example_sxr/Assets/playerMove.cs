using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using MPS.XR;

public class playerMove : MonoBehaviour, ISXRControllerTriggerHandler { 



	private float directionX;
	private float directionY;
    private int score;
    bool left;
    public Text scoreText;
	public float moveSpeed;
    private bool firstClick;

    // Use this for initialization
    void Start ()
	 {
        left = false;
        score = 0;
		moveSpeed = 2f;
        firstClick = false;
        SXRInputModule.Register(this, gameObject);
    }

    // when trigger is released
    public void OnTriggerUp()
    {
        if (!firstClick)
        {
            directionX = 0;
            directionY = 1;
            firstClick = true;
        }
    }
    // when trigger is pressed 
    public void OnTriggerDown()
    {
        if (directionY == 1 && directionX == 0 && left)
        {
            directionY = 0;
            directionX = 1;
            left = false;
        }
        else if (directionX == 0 && directionY == 1 && !left)
        {
            directionY = 0;
            directionX = -1;
            left = true;
        }
        else
        {
            directionY = -1;
            directionX = 0;
        }
  
    }
    // when trigger is pressed and released within a time threshold.
    public void OnTriggerClicked()
    {
        print("WORKING!");
        directionX = 0;
        directionY = 1;
        /*
        if (!firstClick)
        {
            directionX = 0;
            directionY = 1;
            firstClick = true;
        }
        else
        {
           
            if (directionX == 0)
            {
                directionX = -1;
                directionY = 0;
            }
            else
            {
                directionX = 0;
                directionY = 1;
            }
        }
        */
    }

    void setScoreText()
    {
        scoreText.text = "Score: " + score.ToString();
    }

    // Update is called once per frame
    void Update()
    {
        //OnTouchpadSwipe(sD);
        //directionX = directionX * 2;
        //directionY = directionY * 2;
        /*
        if (Input.GetAxis("Horizontal") > 0)
        {
            gameObject.GetComponent<BoxCollider2D>().offset = new Vector2(1, 0);
            directionX = 1;
            directionY = 0;

        }
        if (Input.GetAxis("Horizontal") < 0)
        {
            gameObject.GetComponent<BoxCollider2D>().offset = new Vector2(-1, 0);
            directionX = -1;
            directionY = 0;
        }
        if (Input.GetAxis("Vertical") > 0)
        {
            gameObject.GetComponent<BoxCollider2D>().offset = new Vector2(0, 1);
            directionY = 1;
            directionX = 0;
        }
        if (Input.GetAxis("Vertical") < 0)
        {
            gameObject.GetComponent<BoxCollider2D>().offset = new Vector2(0, -1);
            directionY = -1;
            directionX = 0;
        }
        */
        //transform.Translate(moveSpeed*Input.GetAxis("Horizontal")*Time.deltaTime,moveSpeed*Input.GetAxis("Vertical")*Time.deltaTime, 0f);
       // OnTriggerClicked();
        transform.Translate(moveSpeed * directionX * Time.deltaTime, moveSpeed * directionY * Time.deltaTime, 0f);
        score += 1;
        if (score % 100 == 0)
            setScoreText();
    }
}
