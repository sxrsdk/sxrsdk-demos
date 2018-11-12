using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.Threading;

public class ScoreManager : MonoBehaviour
{
    public GUIText score;

    private int currentScore = 0;
    public int scoreToUpdate = 0;

    private Stack<int> stack;

    private Thread t1;
    private bool isDone = false;

    void Awake()
    {
        stack = new Stack<int>();
        t1 = new Thread(updateScore);
        t1.Start();
    }

    private void updateScore()
    {
        while (true)
        {
            if (stack.Count > 0)
            {
                int newScore = stack.Pop() + currentScore;

                for (int i = currentScore; i <= newScore; i++)
                {
                    scoreToUpdate = i;
                    Thread.Sleep(100); // Change this number if it is too slow.
                }

                currentScore = scoreToUpdate;
            }

            if (isDone)
                return;
        }
    }

    void Update()
    {
        score.text = scoreToUpdate + "";
    }

    public void addScore(int point)
    {
        stack.Push(point);
    }

    public void OnApplicationQuit()
    {
        isDone = true;
        t1.Abort();
    }
}