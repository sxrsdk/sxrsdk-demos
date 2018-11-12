﻿/* Copyright 2018 Samsung Electronics Co., LTD
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

using UnityEngine;
using UnityEngine.EventSystems;

namespace MPS.XR
{
	public interface ISXRControllerTouchpadHandler : IEventSystemHandler
	{
		void OnTouchpadOn(SXRInputModule.TouchpadDirection direction);
		void OnTouchpadOff(SXRInputModule.TouchpadDirection direction);
		void OnTouchpadMove(Vector2 position);
		void OnTouchpadSwipe(SXRInputModule.SwipeDirection swipeDirection);
	}
}