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
package org.marssa.demonstrator.tests.control;

import org.marssa.footprint.datatypes.MBoolean;
import org.marssa.footprint.datatypes.decimal.MDecimal;
import org.marssa.footprint.datatypes.integer.MInteger;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.interfaces.control.rudder.IRudderController;
import org.marssa.services.diagnostics.daq.LabJackUE9;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class RudderControllerTest implements IRudderController{
	private static final Logger logger = (Logger) LoggerFactory
			.getLogger(RudderControllerTest.class.getName());
	
	private static MDecimal angle = new MDecimal(0);
	private IRudderController rudderPhysical;
	private LabJackUE9 lj;
	private double rudderAngle =0;
	
	public RudderControllerTest(LabJackUE9 lj, IRudderController rudderController) throws NoConnection,
			InterruptedException {
		this.lj = lj;
		rotate(new MBoolean(false));
		rotate(new MBoolean(true));
		rudderPhysical = rudderController;
	}

	public double getRudderAngle()
	{
		return rudderAngle;
	}
	/**
	 * The rotateMultiple is used to use the rotate method multiple times
	 */
	public synchronized void rotateMultiple(MInteger multiple,
			MBoolean direction) throws InterruptedException, NoConnection {
		
		rudderPhysical.rotateMultiple(multiple, direction);
		if (direction.getValue())
		{
			logger.info("Rotating Rudder Right" + multiple);
			rudderAngle = rudderAngle + multiple.intValue();
		}else
		{
			logger.info("Rotating Rudder Left" + multiple);
			rudderAngle = rudderAngle - multiple.intValue();
		}
		
	}

	
	public void rotateToCentre() throws NoConnection, InterruptedException {
		rudderPhysical.rotateToCentre();
		logger.info("Rotating Rudder to the Centre");
		rudderAngle = 0;
	}

	/**
	 * The rotate is used to rotate the stepper motor by one step in either left
	 * or right direction The MBoolean direction false means that the rudder has
	 * negative angle (turns the boat to the left direction) The MBoolean
	 * direction true means that the rudder has positive angle (turns the boat
	 * to the right direction)
	 */
	@Override
	public void rotate(MBoolean direction) throws NoConnection,
			InterruptedException {
		
	}

	/**
	 * The getAngle returns the actual angle of the rudder
	 */
	@Override
	public MDecimal getAngle() throws NoConnection {
		return angle;
	}

	@Override
	public void rotateExtreme(MBoolean arg0) throws InterruptedException,
			NoConnection {
		// TODO Auto-generated method stub
		
	}





}
