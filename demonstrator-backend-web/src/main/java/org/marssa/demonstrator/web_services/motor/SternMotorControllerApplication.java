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

import java.util.ArrayList;

import org.marssa.demonstrator.control.electrical_motor.SternDriveMotorController;
import org.marssa.footprint.exceptions.ConfigurationError;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.exceptions.OutOfRange;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.routing.Router;

public class SternMotorControllerApplication extends Application {

	private final ArrayList<CacheDirective> cacheDirectives;
	private final SternDriveMotorController motorController;

	public SternMotorControllerApplication(
			ArrayList<CacheDirective> cacheDirectives,
			SternDriveMotorController motorController) {
		this.cacheDirectives = cacheDirectives;
		this.motorController = motorController;
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());

		// Create the motor speed control handler
		Restlet turnOffMotor = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				response.setCacheDirectives(cacheDirectives);
				try {
					motorController.stop();
				} catch (NumberFormatException e) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
							"The value of the speed resource has an incorrect format");
				} catch (NoConnection e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"There is no connection");
					e.printStackTrace();
				}
			}
		};

		// Create the increase motor speed control handler
		Restlet increaseSpeed = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				response.setCacheDirectives(cacheDirectives);
				try {
					motorController.increase();
					response.setEntity("Increasing motor speed by " + "%",
							MediaType.TEXT_PLAIN);
				} catch (NumberFormatException e) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
							"The value of the speed resource has an incorrect format");
				} catch (InterruptedException e) {
					response.setStatus(Status.INFO_PROCESSING,
							"The ramping algorithm has been interrupted");
					e.printStackTrace();
				} catch (ConfigurationError e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"The request has returned a ConfigurationError");
					e.printStackTrace();
				} catch (OutOfRange e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"The specified value is out of range");
					e.printStackTrace();
				} catch (NoConnection e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"No connection error has been returned");
					e.printStackTrace();
				}
			}
		};

		// Create the decrease motor speed control handler
		Restlet decreaseSpeed = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				response.setCacheDirectives(cacheDirectives);
				try {
					motorController.decrease();
					response.setEntity("Decreasing motor speed by " + "%",
							MediaType.TEXT_PLAIN);
				} catch (NumberFormatException e) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
							"The value of the speed resource has an incorrect format");
				} catch (InterruptedException e) {
					response.setStatus(Status.INFO_PROCESSING,
							"The ramping routinee has been interrupted");
					e.printStackTrace();
				} catch (ConfigurationError e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"The request has returned a ConfigurationError");
					e.printStackTrace();
				} catch (OutOfRange e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"The specified value is out of range");
					e.printStackTrace();
				} catch (NoConnection e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL,
							"No connection error has been returned");
					e.printStackTrace();
				}
			}
		};

		// Create the motor speed monitoring handler
		Restlet speedMonitor = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				response.setCacheDirectives(cacheDirectives);
				response.setEntity(motorController.getSpeed().toString(),
						MediaType.TEXT_PLAIN);
			}
		};

		router.attach("/stop", turnOffMotor);
		router.attach("/increaseSpeed", increaseSpeed);
		router.attach("/decreaseSpeed", decreaseSpeed);
		router.attach("/speed", speedMonitor);

		return router;
	}
}