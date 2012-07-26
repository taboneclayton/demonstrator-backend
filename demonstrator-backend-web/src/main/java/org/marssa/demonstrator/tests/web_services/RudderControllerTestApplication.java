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
package org.marssa.demonstrator.tests.web_services;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.marssa.footprint.datatypes.decimal.MDecimal;
import org.marssa.footprint.exceptions.NoConnection;
import org.restlet.Application;

@Path("/test/rudder")
@RequestScoped
public class RudderControllerTestApplication extends Application {

	@GET
	@Produces("application/json")
	public String getAngle() {
		return WebServicesTest.getRudderAngle().toJSON().toString();
	}

	@POST
	@Produces("text/plain")
	@Path("/centre")
	public String centre() throws NoConnection, InterruptedException {
		WebServicesTest.setRudderAngle(new MDecimal(0));
		return "Rotating the rudder to the centre";
	}

	@POST
	@Produces("text/plain")
	@Path("/rotate/{direction}")
	public String rotate(@PathParam("direction") boolean direction) {
		if (direction)
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() + 1.0));
		else
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() - 1.0));
		return "Rotating the rudder in the direction set by direction = "
				+ direction;
	}

	@POST
	@Produces("text/plain")
	@Path("/rotateMore/{direction}")
	public String rotateMore(@PathParam("direction") boolean direction) {
		if (direction)
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() + 5.0));
		else
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() - 5.0));
		return "Rotating the rudder more in the direction set by = "
				+ direction;
	}

	@POST
	@Produces("text/plain")
	@Path("/rotateFull/{direction}")
	public String rotateFull(@PathParam("direction") boolean direction) {
		if (direction)
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() + 30.0));
		else
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() - 30.0));
		return "Rotating the rudder to the extreme in the direction set by = "
				+ direction;
	}

	@POST
	@Produces("text/plain")
	@Path("/rotate/{direction}/{amount}")
	public String rotate(@PathParam("direction") boolean direction,
			@PathParam("amount") double amount) {
		if (direction)
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() + amount));
		else
			WebServicesTest.setRudderAngle(new MDecimal(WebServicesTest
					.getRudderAngle().doubleValue() - amount));
		return "Rotating the rudder by " + amount
				+ " in the direction set by direction = " + direction;
	}
}