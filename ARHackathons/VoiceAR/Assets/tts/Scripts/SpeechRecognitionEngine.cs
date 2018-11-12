using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Windows.Speech;

public class SpeechRecognitionEngine : MonoBehaviour
{
    //public string[] vkeys = new string[] { "rotate 30", "rotate 45", "rotate 90", "rotate 180", "rotate 360"  };
    public string[] keywords = new string[] { "up", "down", "left", "right", "stop" };
    public ConfidenceLevel confidence = ConfidenceLevel.Medium;
    public float speed = 1;

    public Text results;
    public Image target;
    public GameObject heart;

    protected PhraseRecognizer recognizer;
    protected string word = "right";

    private void Start()
    {
        //GameObject ngo = new GameObject("myTextGO");
        //ngo.transform.SetParent(this.transform);

        //Text myText = ngo.AddComponent<Text>();
        //myText.text = "Ta-dah!";

        if (keywords != null)
        {
            recognizer = new KeywordRecognizer(keywords, confidence);
            recognizer.OnPhraseRecognized += Recognizer_OnPhraseRecognized;
            recognizer.Start();
        }
    }

    private void Recognizer_OnPhraseRecognized(PhraseRecognizedEventArgs args)
    {
        word = args.text;
        results.text = "You said: <b>" + word + "</b>";
    }

    private void Update()
    {
        float x = 0;
        float y = 0;

        switch (word)
        {
            case "up":
                y += speed;
                break;
            case "down":
                y -= speed;
                break;
            case "left":
                x -= speed;
                break;
            case "right":
                x += speed;
                break;
            case "rotate":
                spriteRotate();
                break;
            case "stop":
                x = 0;
                y = 0;
                break;
            case "zoom in":
                heart.transform.localScale += new Vector3(1,1,1);
                word = "";
                break;
            case "zoom out":
                heart.transform.localScale -= new Vector3(.8f, .8f, .8f);
                word = "";
                break;
        }

        target.transform.position += new Vector3(x, y, 0);
    }


    private void spriteRotate()
         //private void spriteRotate(string angle)
    {
        heart.transform.Rotate(new Vector3(0,0,1), 0.5f);

        if (Input.GetKeyDown(KeyCode.LeftArrow))
        {
            // left
            heart.transform.Rotate(-1.0f, 0.0f, 0.0f);  // does nothing, just a bad guess
        }

        if (Input.GetKeyDown(KeyCode.RightArrow))
        {
            // right
            heart.transform.Rotate(1.0f, 0.0f, 0.0f);  // does nothing, just a bad guess
        }
    }

    private void OnApplicationQuit()
    {
        if (recognizer != null && recognizer.IsRunning)
        {
            recognizer.OnPhraseRecognized -= Recognizer_OnPhraseRecognized;
            recognizer.Stop();
        }
    }
}
