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

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.marssa.demonstrator.constants.Constants;
import org.marssa.demonstrator.control.electrical_motor.SternDriveMotorController;
import org.marssa.demonstrator.control.lighting.NavigationLightsController;
import org.marssa.demonstrator.control.lighting.UnderwaterLightsController;
import org.marssa.demonstrator.control.path_planning.PathPlanningController;
import org.marssa.demonstrator.control.rudder.RudderController;
import org.marssa.demonstrator.tests.control.GPSReceiverTest;
import org.marssa.demonstrator.tests.control.RudderControllerTest;
import org.marssa.demonstrator.web_services.StaticFileServerApplication;
import org.marssa.footprint.datatypes.MBoolean;
import org.marssa.footprint.datatypes.decimal.MDecimal;
import org.marssa.footprint.exceptions.ConfigurationError;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.exceptions.OutOfRange;
import org.marssa.footprint.interfaces.control.rudder.IRudderController;
import org.marssa.services.diagnostics.daq.LabJackUE9;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.CacheDirective;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServicesTest {
	private static final Logger logger = LoggerFactory
			.getLogger(WebServicesTest.class);
	private static final ArrayList<CacheDirective> cacheDirectives = new ArrayList<CacheDirective>();

	private static SternDriveMotorController sternMotorController;
	private static PathPlanningController pathPlanningController;
	private static GPSReceiverTest gpsReceiver;
	private static RudderControllerTest rudderController;
	private static IRudderController rudderControllerPhysical;

	static LabJackUE9 labJackue9;
	NavigationLightsController navLightsController;
	UnderwaterLightsController underwaterLightsController;

	static class LightState {
		public MBoolean navLightState = new MBoolean(false);
		public MBoolean underwaterLightState = new MBoolean(false);
	}

	private static LightState lightState = new LightState();
	private static MDecimal rudderAngle = new MDecimal(0.0f);

	public static MDecimal getRudderAngle() {
		return rudderAngle;
	}

	public static void setRudderAngle(MDecimal newRudderAngle) {
		rudderAngle = newRudderAngle;
	}

	public static LightState getLightState() {
		return lightState;
	}

	public static void setLightState(LightState newLightState) {
		lightState = newLightState;
	}

	/**
	 * @param args
	 *            the args
	 * @throws NoConnection
	 * @throws UnknownHostException
	 * @throws OutOfRange
	 * @throws ConfigurationError
	 * @throws InterruptedException
	 */
	public static void main(java.lang.String[] args)
			throws UnknownHostException, NoConnection, ConfigurationError,
			OutOfRange, InterruptedException {

		logger.info("Initialising LabJack ...");
		labJackue9 = LabJackUE9.getInstance(Constants.LABJACKUE9.HOST,
				Constants.LABJACKUE9.PORT);
		logger.info("LabJack initialized successfully on {}:{}",
				Constants.LABJACKUE9.HOST, Constants.LABJACKUE9.PORT);

		logger.info("Initialising motor controller ... ");
		sternMotorController = new SternDriveMotorController(labJackue9);
		logger.info("Motor controller initialised successfully");

		logger.info("Initialising rudder controller ... ");
		rudderControllerPhysical = new RudderController(labJackue9);
		rudderController = new RudderControllerTest(labJackue9,
				rudderControllerPhysical);
		logger.info("Rudder controller initialised successfully");

		logger.info("Initialising GPS ... ");
		gpsReceiver = new GPSReceiverTest(rudderController);
		logger.info("GPS controller initialised successfully");

		logger.info("Initialising Path Planning controller ... ");
		pathPlanningController = new PathPlanningController(
				sternMotorController, rudderController, gpsReceiver);
		// pathPlanningController = new PathPlanningController(null, null,null);
		logger.info("Path Planning controller initialised successfully");

		// Create a new Component
		Component component = new Component();

		System.out.println("Starting Web Services on "
				+ Constants.WEB_SERVICES.HOST.getContents() + ":"
				+ Constants.WEB_SERVICES.PORT.toString() + " ...");

		cacheDirectives.add(CacheDirective.noCache());
		cacheDirectives.add(CacheDirective.noStore());

		// Add a new HTTP server listening on the given port
		Server server = component.getServers().add(Protocol.HTTP,
				Constants.WEB_SERVICES.HOST.getContents(),
				Constants.WEB_SERVICES.PORT.intValue());
		server.getContext()
				.getParameters()
				.add("maxTotalConnections",
						Constants.WEB_SERVICES.MAX_TOTAL_CONNECTIONS.toString());

		// Add new client connector for the FILE protocol
		component.getClients().add(Protocol.FILE);

		// Attach the static file server application
		component.getDefaultHost()
				.attach("", new StaticFileServerApplication());

		// Attach the Stern control application
		component.getDefaultHost().attach("/sternMotor",
				new SternMotorControllerTestApplication(cacheDirectives));

		// Attach the rudder control application
		component.getDefaultHost().attach("/rudder",
				new RudderControllerTestApplication(cacheDirectives));

		// Attach the GPS receiver application
		component.getDefaultHost().attach("/gps",
				new GPSReceiverTestApplication(cacheDirectives));

		// Attach the path planner application
		component.getDefaultHost().attach(
				"/pathPlanner",
				new PathControllerTestApplication(cacheDirectives,
						pathPlanningController));

		// Start the component
		try {
			component.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}