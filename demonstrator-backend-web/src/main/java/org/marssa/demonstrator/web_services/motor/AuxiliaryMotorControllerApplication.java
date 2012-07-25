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
package org.marssa.demonstrator.web_services.motor;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.marssa.demonstrator.beans.MotorControllerBean;
import org.marssa.footprint.datatypes.decimal.MDecimal;
import org.marssa.footprint.exceptions.ConfigurationError;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.exceptions.OutOfRange;

@Path("/motors/auxiliary")
public class AuxiliaryMotorControllerApplication {

	@Inject
	private MotorControllerBean motorControllerBean;

	@GET
	@Produces("application/json")
	@Path("/speed")
	public String getSpeed() throws InterruptedException, ConfigurationError,
			OutOfRange, NoConnection {
		return motorControllerBean.getAuxMotorsController().getSpeed().toJSON()
				.toString();
	}

	@POST
	@Produces("text/plain")
	@Path("/stop")
	public String stop() throws NoConnection {
		motorControllerBean.getAuxMotorsController().stop();
		return "Stopped auxiliary motor";
	}

	@POST
	@Produces("text/plain")
	@Path("/speed/increase")
	public String increaseSpeed() throws InterruptedException,
			ConfigurationError, OutOfRange, NoConnection {
		motorControllerBean.getSternDriveMotorController().increase();
		return "Increased auxiliary motor speed";
	}

	@POST
	@Produces("text/plain")
	@Path("/speed/decrease")
	public String decreaseSpeed() throws InterruptedException,
			ConfigurationError, OutOfRange, NoConnection {
		motorControllerBean.getSternDriveMotorController().decrease();
		return "Decreased auxiliary motor speed";
	}

	@POST
	@Produces("text/plain")
	@Path("/speed/{speed}")
	public String setSpeed(@PathParam("speed") String speed)
			throws NoConnection, InterruptedException, ConfigurationError,
			OutOfRange {
		double value = Double.parseDouble(speed);
		motorControllerBean.getAuxMotorsController()
				.rampTo(new MDecimal(value));
		return "Set auxiliary motor speed to " + speed + "%";
	}
}