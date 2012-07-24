/**
 * Copyright 2012 MARSEC-XL International Limited
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
package org.marssa.demonstrator.web_services.path_planning;

import java.util.concurrent.ConcurrentMap;

import org.marssa.demonstrator.control.path_planning.Waypoint;
import org.restlet.resource.ServerResource;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 * 
 */
public abstract class BaseResource extends ServerResource {

	/**
	 * Returns the map of items managed by this application.
	 * 
	 * @return the map of items managed by this application.
	 */
	protected ConcurrentMap<String, Waypoint> getWaypoints() {
		// TODO changed from PathControllerTestApplication
		// This was done since test scope is not visible from here
		return ((PathControllerApplication) getApplication()).getWaypoints();
		// Change back when on plug computer
	}

}