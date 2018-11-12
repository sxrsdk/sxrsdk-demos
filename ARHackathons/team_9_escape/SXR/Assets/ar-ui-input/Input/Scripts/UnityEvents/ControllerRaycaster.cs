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
 
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;


namespace MPS.XR
{
	public class ControllerRaycaster : BaseRaycaster
	{
		private Camera _eventCamera;
		public float maxDistance = 500;
		[SerializeField] protected LayerMask m_EventMask = -1;

		
		public LayerMask eventMask
		{
			get { return this.m_EventMask; }
			set { this.m_EventMask = value; }
		}

		public override void Raycast(PointerEventData eventData, List<RaycastResult> resultAppendList)
		{
			RaycastHit hit;
			if (Physics.Raycast(transform.position, transform.TransformDirection(Vector3.forward), out hit, maxDistance,
				eventMask))
			{
				Debug.DrawRay(transform.position, transform.TransformDirection(Vector3.forward) * hit.distance, Color.yellow);
				RaycastResult raycastResult = new RaycastResult()
				{
					gameObject = hit.collider.gameObject,
					module = (BaseRaycaster) this,
					distance = hit.distance,
					worldPosition = hit.point,
					worldNormal = hit.normal,
					screenPosition = eventData.position,
					index = (float) resultAppendList.Count,
					sortingLayer = 0,
					sortingOrder = 0
				};
				resultAppendList.Add(raycastResult);
			}
		}

		public override Camera eventCamera
		{
			get { return _eventCamera; }
		}
	}
}