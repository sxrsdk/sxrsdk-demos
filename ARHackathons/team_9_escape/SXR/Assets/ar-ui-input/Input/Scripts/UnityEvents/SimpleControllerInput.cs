/* Copyright 2018 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using Sxr;

namespace MPS.XR
{
    public class SimpleControllerInput
    {
        private SXRManager gvrm;
        private SXRControllerManager controller;
        private SXRSimpleController simpleController;

        public float controllerClickThreshold = 0.5f;
        
        //simple controller fields
        private bool isTriggerDown;

        private float triggerDownTime = 0;
        
        // Clicking Down the TouchPadButton
        private bool isTouchButtonDown;

        private float touchButtonDownTime = 0;
        
        // Clicking Down the Backbutton
        private bool isBackButtonDown;

        // Clicking Down the HomeButton
        private bool isHomeButtonDown = false;

        private float homeDownTime = 0;

        // Clicking Down the VolumeUp Button
        private bool isVolumeUpButtonDown = false;

        // Clicking Down the VolumeDown Button
        private bool isVolumeDownButtonDown = false;

        // Trigger Button Down
        private bool isTriggerButtonDown = false;

        // volume buttons down
        private bool isVolPlusDown;

        private bool isvolMinusDown;

        //touchpad and swipe variables
        private int previousTouchpadState = 0;
        private Vector2 initialTouchPosition = Vector2.zero;

        private Vector2
            prevTouchPosition = Vector2.zero; //necessary because this goes to 0,0 as soon as finger is removed

        private int minSwipeThreshold = 20; //min amount of movement for swipe
        private float maxSwipeTime = 0.5f; //max amount of time for swipe
        private float swipeStarttime = 0;
        private float homeButtonDownTime = 0;
        private bool keyboardEmulation;
        private static List<GameObject> RegisteredTriggerHandlers = new List<GameObject>();
        private static List<GameObject> RegisteredBackButtonHandlers = new List<GameObject>();
        private static List<GameObject> RegisteredTouchpadButtonHandlers = new List<GameObject>();
        private static List<GameObject> RegisteredTouchpadHandlers = new List<GameObject>();
        private static List<GameObject> RegisteredOrientationHandlers = new List<GameObject>();
        private static List<GameObject> RegisteredHomeButtonHandlers = new List<GameObject>();
        private static List<GameObject> RegisteredVolumeButtonHandlers = new List<GameObject>();

        public SimpleControllerInput(bool _keyboardEmulation)
        {
            keyboardEmulation = _keyboardEmulation;
            gvrm = SXRManager.Instance;
            controller = gvrm.getControllerManager();
            simpleController = controller.getSimpleController();
        }

        //Registers game object for global controller events. Must check all as a script may have more than one handler.
        public static void Register(IEventSystemHandler handler, GameObject go)
        {
            if (handler is ISXRControllerTriggerHandler)
            {
                RegisteredTriggerHandlers.Add(go);
            }

            if (handler is ISXRControllerBackHandler)
            {
                RegisteredBackButtonHandlers.Add(go);
            }

            if (handler is ISXRControllerHomeHandler)
            {
                RegisteredHomeButtonHandlers.Add(go);
            }
            
            if (handler is ISXRControllerTouchpadButtonHandler)
            {
                RegisteredTouchpadButtonHandlers.Add(go);
            }
            if (handler is ISXRControllerTouchpadHandler)
            {
                RegisteredTouchpadHandlers.Add(go);
            }
            if (handler is ISXRControllerGyroHandler)
            {
                RegisteredOrientationHandlers.Add(go);
            }
            if (handler is ISXRControllerVolumeHandler)
            {
                RegisteredVolumeButtonHandlers.Add(go);
            }
        }

        public void Process(GameObject selectedObject)
        {
            SXRControllerPluginIF.SxrControllerState controllerState = simpleController.getState();
            if (keyboardEmulation)
            {
                controllerState = KeyboardEmulation.Emulate(controllerState);
            }
            HandleTriggerState(controllerState);
            HandleTouchButtonState(controllerState);
            HandleBackButtonState(controllerState);
            HandleHomeButtonState(controllerState);
            HandleVolumeButtonState(controllerState);
            HandleTouchpadState(controllerState, selectedObject);
            HandleGyroState(controllerState);
        }
        
        private void HandleGyroState(SXRControllerPluginIF.SxrControllerState controllerState)
        {
                ExecuteEventGlobal<ISXRControllerGyroHandler>(RegisteredOrientationHandlers,
                    (target, eventData) => { target.OnOrientation(Quaternion.Inverse(controllerState.orientation)); });
        }
        
        private void HandleTouchpadState(SXRControllerPluginIF.SxrControllerState controllerState, GameObject selectedObj)
        {
            if (controllerState.touchState == 1) //pad is being touched
            {
                if (previousTouchpadState == 0)
                {
                    initialTouchPosition = controllerState.touchPosition;
                    swipeStarttime = Time.unscaledTime;
                        ExecuteEventGlobal<ISXRControllerTouchpadHandler>(RegisteredTouchpadHandlers,
                            (target, eventData) => { target.OnTouchpadOn(getTouchpadDirection(controllerState)); });
                }
                //normalize movement to -1.0-1.0
                Vector2 normalizedPosition = controllerState.touchPosition;
                Vector2 controllerMultiplier =
                    SXRInputModule.TouchpadMultipliers[SXRInputModule.TouchpadDevice.CONTROLLER];
                normalizedPosition.x /= controllerMultiplier.x;
                normalizedPosition.y /= controllerMultiplier.y;
                ExecuteEventGlobal<ISXRControllerTouchpadHandler>(RegisteredTouchpadHandlers,
                    (target, eventData) => { target.OnTouchpadMove(normalizedPosition); });
                prevTouchPosition = controllerState.touchPosition;
            }
            else if (controllerState.touchState == 0)
            {
                if (previousTouchpadState == 1)
                {
                    ExecuteEventGlobal<ISXRControllerTouchpadHandler>(RegisteredTouchpadHandlers,
                        (target, eventData) => { target.OnTouchpadOff(getTouchpadDirection(prevTouchPosition)); });
                    //Handle Swipe Gestures
                    HandleTouchSwipe(prevTouchPosition, selectedObj);
                }
            }
            previousTouchpadState = controllerState.touchState;
        }
        
        private void HandleTouchSwipe(Vector2 _touchPos, GameObject selectedObj)
        {
            if ((Time.unscaledTime - swipeStarttime) > maxSwipeTime)
            {
                return;
            }

            SXRInputModule.SwipeDirection swipeDir = SXRInputModule.SwipeDirection.UNKNOWN;
            //print("initialTouch=" + initialTouchPosition.ToString() + ", latestTouch=" + _touchPos.ToString());
            float xDiff = (initialTouchPosition.x - _touchPos.x);
            float yDiff = (initialTouchPosition.y - _touchPos.y);

            if (Mathf.Abs(xDiff) > Mathf.Abs(yDiff))
            {
                //right / left swipe
                if (Mathf.Abs(xDiff) < minSwipeThreshold)
                {
                    return;
                }
                if (xDiff > 0)
                {
                    swipeDir = SXRInputModule.SwipeDirection.LEFT;
                }
                else
                {
                    swipeDir = SXRInputModule.SwipeDirection.RIGHT;
                }
            }
            else
            {
                //up / down swipe
                if (Mathf.Abs(yDiff) < minSwipeThreshold)
                {
                    return;
                }
                if (yDiff > 0)
                {
                    swipeDir = SXRInputModule.SwipeDirection.UP;
                }
                else
                {
                    swipeDir = SXRInputModule.SwipeDirection.DOWN;
                }
            }
            ExecuteEventSelected<ISXRControllerTouchpadHandler>(selectedObj,
                            (target, eventData) => { target.OnTouchpadSwipe(swipeDir); });
            ExecuteEventGlobal<ISXRControllerTouchpadHandler>(RegisteredTouchpadHandlers,
                (target, eventData) => { target.OnTouchpadSwipe(swipeDir); });
            
        }

        private void HandleBackButtonState(SXRControllerPluginIF.SxrControllerState controllerState)
        {
            if (controllerState.buttonBackDown)
            {
                if (!isBackButtonDown)
                {
                    isBackButtonDown = true;
                    ExecuteEventGlobal<ISXRControllerBackHandler>(RegisteredBackButtonHandlers,
                        (target, eventData) => { target.OnBackButtonDown(); });
                }
            }
            else
            {
                if (isBackButtonDown)
                {
                    isBackButtonDown = false;
                    ExecuteEventGlobal<ISXRControllerBackHandler>(RegisteredBackButtonHandlers,
                        (target, eventData) => { target.OnBackButtonUp(); });
                }
            }
        }

        private bool isVolUpButtonDown;
        private bool isVolDownButtonDown;
        
        private void HandleVolumeButtonState(SXRControllerPluginIF.SxrControllerState controllerState)
        {
            //volume increase
            if (controllerState.buttonVolumeUpDown)
            {
                if (!isVolUpButtonDown)
                {
                    isVolUpButtonDown = true;
//                    Debug.Log("volume up buttton down");
                }
            }
            else
            {
                if (isVolUpButtonDown)
                {
//                    Debug.Log("volume up buttton up");
                    isVolUpButtonDown = false;
                    ExecuteEventGlobal<ISXRControllerVolumeHandler>(RegisteredVolumeButtonHandlers,
                                    (target, eventData) => { target.OnVolumeUpClicked(); });
                }
            }
            //volume decrease
            if (controllerState.buttonVolumeDownDown)
            {
                if (!isVolDownButtonDown)
                {
                    isVolDownButtonDown = true;
//                    Debug.Log("volume down buttton down");
                }
            }
            else
            {
                if (isVolDownButtonDown)
                {
//                    Debug.Log("volume down buttton up");
                    isVolDownButtonDown = false;
                    ExecuteEventGlobal<ISXRControllerVolumeHandler>(RegisteredVolumeButtonHandlers,
                                    (target, eventData) => { target.OnVolumeDownClicked(); });
                }
            }
        }
        
        private void HandleHomeButtonState(SXRControllerPluginIF.SxrControllerState controllerState)
        {
            if (controllerState.buttonHomeDown)
            {
                if (!isHomeButtonDown)
                {
                    homeButtonDownTime = Time.unscaledTime;
                    isHomeButtonDown = true;
                }
            }
            else
            {
                if (isHomeButtonDown)
                {
                    isHomeButtonDown = false;
                    if (homeButtonDownTime + controllerClickThreshold > Time.unscaledTime)
                    {
                        ExecuteEventGlobal<ISXRControllerHomeHandler>(RegisteredHomeButtonHandlers,
                            (target, eventData) => { target.OnHomeClicked(); });
                    }
                }
            }
        }

        private void HandleTriggerState(SXRControllerPluginIF.SxrControllerState controllerState)
        {
            if (controllerState.buttonTriggerDown)
            {
                if (!isTriggerDown)
                {
                    isTriggerDown = true;
                    triggerDownTime = Time.unscaledTime;
                    ExecuteEventGlobal<ISXRControllerTriggerHandler>(RegisteredTriggerHandlers,
                        (target, eventData) => { target.OnTriggerDown(); });
                }
            }
            else
            {
                if (isTriggerDown)
                {
                    isTriggerDown = false;
                        ExecuteEventGlobal<ISXRControllerTriggerHandler>(RegisteredTriggerHandlers,
                            (target, eventData) => { target.OnTriggerUp(); });
                    //button was clicked if down+up are less than controller click threshold
                    if (triggerDownTime + controllerClickThreshold > Time.unscaledTime)
                    {
                        ExecuteEventGlobal<ISXRControllerTriggerHandler>(RegisteredTriggerHandlers,
                            (target, eventData) => { target.OnTriggerClicked(); });
                    }
                    triggerDownTime = 0;
                }
            }
        }

        private void HandleTouchButtonState(SXRControllerPluginIF.SxrControllerState controllerState)
        {
            if (controllerState.buttonTouchClickDown)
            {
                if (!isTouchButtonDown)
                {
                    isTouchButtonDown = true;
                    touchButtonDownTime = Time.unscaledTime;
                    ExecuteEventGlobal<ISXRControllerTouchpadButtonHandler>(RegisteredTouchpadButtonHandlers,
                        (target, eventData) => { target.OnTouchpadButtonDown(getTouchpadDirection(controllerState)); });
                }
            }
            else
            {
                if (isTouchButtonDown)
                {
                    isTouchButtonDown = false;
                    SXRInputModule.TouchpadDirection touchDirection = getTouchpadDirection(controllerState);
                    ExecuteEventGlobal<ISXRControllerTouchpadButtonHandler>(RegisteredTouchpadButtonHandlers,
                        (target, eventData) => { target.OnTouchpadButtonUp(touchDirection); });
                    //button was clicked if down+up are less than controller click threshold
                    if (touchButtonDownTime + controllerClickThreshold > Time.unscaledTime)
                    {
                        ExecuteEventGlobal<ISXRControllerTouchpadButtonHandler>(RegisteredTouchpadButtonHandlers,
                            (target, eventData) => { target.OnTouchpadClicked(touchDirection); });
                    }
                    touchButtonDownTime = 0;
                }
            }
        }


        private SXRInputModule.TouchpadDirection getTouchpadDirection(
            SXRControllerPluginIF.SxrControllerState controllerState)
        {
            Vector2 touchPos = controllerState.touchPosition;
            return getTouchpadDirection(touchPos);
        }

        private SXRInputModule.TouchpadDirection getTouchpadDirection(Vector2 touchPos)
        {
            SXRInputModule.TouchpadDirection dir = SXRInputModule.TouchpadDirection.UNKNOWN;
            //controller = 0 - 314 on both axis. Offset by 157 so that 157 = 0;
            touchPos += new Vector2(-157, -157);
            if (Mathf.Abs(touchPos.x) < 100 && Mathf.Abs(touchPos.y) < 100)
            {
                dir = SXRInputModule.TouchpadDirection.CENTER;
            }
            else
            {
                if (Mathf.Abs(touchPos.x) > Mathf.Abs(touchPos.y)) //right / left
                {
                    if (touchPos.x > 0)
                    {
                        dir = SXRInputModule.TouchpadDirection.RIGHT;
                    }
                    else
                    {
                        dir = SXRInputModule.TouchpadDirection.LEFT;
                    }
                }
                else //up / down. y axis on touchpad starts from top and moves down.
                {
                    if (touchPos.y < 0)
                    {
                        dir = SXRInputModule.TouchpadDirection.UP;
                    }
                    else
                    {
                        dir = SXRInputModule.TouchpadDirection.DOWN;
                    }
                }
            }

            return dir;
        }

        private void ExecuteEventSelected<T>(GameObject selectedObj, ExecuteEvents.EventFunction<T> lambda) where T : IEventSystemHandler
        {
            if (selectedObj != null && ExecuteEvents.CanHandleEvent<T>(selectedObj))
            {
                ExecuteEvents.Execute<T>(selectedObj, null, lambda);
            } 

        }  
        
        private void ExecuteEventGlobal<T>( List<GameObject> registeredHandlerList, ExecuteEvents.EventFunction<T> lambda) where T : IEventSystemHandler
        {
            foreach (var handler in registeredHandlerList)
            {
                ExecuteEvents.Execute<T>(handler, null, lambda);
            }
        }
        
        public void Destroy()
        {
            RegisteredTriggerHandlers.Clear();
            RegisteredBackButtonHandlers.Clear();
            RegisteredHomeButtonHandlers.Clear();
            RegisteredOrientationHandlers.Clear();
            RegisteredTouchpadButtonHandlers.Clear();
            RegisteredTouchpadHandlers.Clear();
            RegisteredVolumeButtonHandlers.Clear();
        } 
    }
}