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
using Sxr;
using UnityEngine;

namespace MPS.XR
{
    public class KeyboardEmulation
    {
        private static Vector3 dummyRotation = Vector3.zero;
        private static Vector2 dummyTouchpad = Vector2.zero;
        static bool touchpadDown = false;
        private static bool isHomeDown = false;
        private static float homeDownTime = 0;
        private static Vector2 TouchpadMovestart = new Vector2(157, 157); //midpoint on gvr touchpad
        private static bool isInit;
        
        public static void Init()
        {
            Debug.Log("***SXR Keyboard Emulation -\n      keypad=touchpad, space=touchpad button, enter=trigger button, WASD=orientation, Esc=back, H=Home, +/- = volume");
            isInit = true;
        }
        
        public static SXRControllerPluginIF.SxrControllerState Emulate(
                        SXRControllerPluginIF.SxrControllerState controllerState)
        {
            if (!isInit)
            {
                Init();
            }
            //trigger
            if (Input.GetKey(KeyCode.Return) || Input.GetKey(KeyCode.KeypadEnter))
            {
                controllerState.buttonTriggerDown = true;
            }

            // back button
            if (UnityEngine.Input.GetKey(KeyCode.Escape))
            {
                controllerState.buttonBackDown = true;
            }
            
            // home button
            if (UnityEngine.Input.GetKey(KeyCode.H))
            {
                controllerState.buttonHomeDown = true;
            }

            //touchpad button
            if (UnityEngine.Input.GetKey(KeyCode.Space))
            {
                controllerState.buttonTouchClickDown = true;
            }
            
            //volume button
            if (UnityEngine.Input.GetKey(KeyCode.Plus) || UnityEngine.Input.GetKey(KeyCode.Equals) )
            {
                controllerState.buttonVolumeUpDown = true;
            } else if (UnityEngine.Input.GetKey(KeyCode.Minus) || UnityEngine.Input.GetKey(KeyCode.Underscore) )
            {
                controllerState.buttonVolumeDownDown = true;
            } 
            
            if (Application.isEditor) //this will cause problems on android device
            {
                //touchpad movement
                bool touchpadOn = false;
                if (Input.GetKey(KeyCode.LeftArrow))
                {
                    TouchMove(-10.5f, 0);
                    touchpadOn = true;
                }

                if (Input.GetKey(KeyCode.RightArrow))
                {
                    TouchMove(10.5f, 0);
                    touchpadOn = true;
                }

                if (Input.GetKey(KeyCode.UpArrow))
                {
                    TouchMove(0, -10.5f);
                    touchpadOn = true;
                }

                if (Input.GetKey(KeyCode.DownArrow))
                {
                    TouchMove(0, 10.5f);
                    touchpadOn = true;
                }

                if (!touchpadOn)
                {
                    TouchUp();
                }

                controllerState.touchState = touchpadDown ? 1 : 0;
                controllerState.touchPosition = dummyTouchpad;

                //orientation emulation
                if (Input.GetKey(KeyCode.A))
                {
                    dummyRotation.z -= 1;
                }

                if (Input.GetKey(KeyCode.D))
                {
                    dummyRotation.z += 1;
                }

                if (Input.GetKey(KeyCode.W))
                {
                    dummyRotation.x += 1;
                }

                if (Input.GetKey(KeyCode.S))
                {
                    dummyRotation.x -= 1;
                }

                controllerState.orientation = Quaternion.Euler(dummyRotation.x, dummyRotation.y, dummyRotation.z);
            }

            return controllerState;
        }

        private static void TouchUp()
        {
            touchpadDown = false;
            dummyTouchpad = TouchpadMovestart;
        }

        private static void TouchMove(float x, float y)
        {
            if (!touchpadDown)
            {
                touchpadDown = true;
                dummyTouchpad = TouchpadMovestart;
            }

            dummyTouchpad.x += x;
            dummyTouchpad.y += y;
        }
    }
}