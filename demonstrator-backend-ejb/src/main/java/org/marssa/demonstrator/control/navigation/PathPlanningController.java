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
package org.marssa.demonstrator.control.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.marssa.demonstrator.constants.Constants;
import org.marssa.footprint.datatypes.MBoolean;
import org.marssa.footprint.datatypes.composite.Coordinate;
import org.marssa.footprint.datatypes.composite.Latitude;
import org.marssa.footprint.datatypes.composite.Longitude;
import org.marssa.footprint.datatypes.decimal.DegreesDecimal;
import org.marssa.footprint.datatypes.integer.MInteger;
import org.marssa.footprint.exceptions.ConfigurationError;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.exceptions.NoValue;
import org.marssa.footprint.exceptions.OutOfRange;
import org.marssa.footprint.interfaces.control.motor.IMotorController;
import org.marssa.footprint.interfaces.control.rudder.IRudderController;
import org.marssa.footprint.interfaces.navigation.IGpsReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Clayton Tabone
 * 
 */
public class PathPlanningController {

	private static final Logger logger = LoggerFactory
			.getLogger(PathPlanningController.class);

	private final IMotorController motorController;
	private final IRudderController rudderController;
	private final IGpsReceiver gpsReceiver;
	private final TimerService timerService;
	private Timer timer = null;

	private Coordinate currentPositionRead;
	private Coordinate nextHeading;

	private int count = 0;
	private final List<Waypoint> wayPointList = new ArrayList<Waypoint>();
	private final List<Coordinate> path = new ArrayList<Coordinate>();

	private boolean routeReverse = false;

	/**
	 * @throws ConfigurationError
	 * @throws OutOfRange
	 * @throws NoConnection
	 * 
	 */
	public PathPlanningController(IMotorController motorController,
			IRudderController rudderController, IGpsReceiver gpsReceiver,
			TimerService timerService) {
		logger.info("Creating Path Planning Controller ...");
		this.motorController = motorController;
		this.rudderController = rudderController;
		this.gpsReceiver = gpsReceiver;
		this.timerService = timerService;
		logger.info("Created Path Planning Controller");
	}

	public List<Waypoint> getWayPoints() {
		return new ArrayList<Waypoint>(wayPointList);
	}

	public void setWayPoints(List<Waypoint> wayPointList) {
		this.wayPointList.clear();
		this.wayPointList.addAll(wayPointList);
	}

	public List<Coordinate> getPath() {
		return new ArrayList<Coordinate>(path);
	}

	public Coordinate getNextHeading() {
		return nextHeading;
	}

	public void setNextHeading(Coordinate nextHeading) {
		this.nextHeading = nextHeading;
	}

	private void readPosition() throws NoConnection, NoValue, OutOfRange {
		currentPositionRead = gpsReceiver.getCoordinate();
		path.add(currentPositionRead);
	}

	private void shortestAngle(double _currentHeading, double _targetHeading,
			MInteger angleOut) throws NoConnection, NoValue, OutOfRange,
			InterruptedException {
		if (_targetHeading > _currentHeading) {
			if ((_targetHeading - _currentHeading) > 180) {
				rudderController.rotateMultiple(angleOut, new MBoolean(true));
			} else {
				rudderController.rotateMultiple(angleOut, new MBoolean(false));
			}

		} else {
			if ((_currentHeading - _targetHeading) >= 180) {
				rudderController.rotateMultiple(angleOut, new MBoolean(false));
			} else {
				rudderController.rotateMultiple(angleOut, new MBoolean(true));
			}
		}
	}

	// This method is called upon to drive the vessel in the right direction
	public void drive() throws NoConnection, NoValue, OutOfRange,
			InterruptedException {

		double currentHeading = gpsReceiver.getCOG().doubleValue();

		double targetHeading = determineHeading();
		double difference = Math.abs(currentHeading - targetHeading);

		if (difference < Constants.PATH.Path_Accuracy_Lower.doubleValue()) {
			rudderController.rotateToCentre();
		}
		if ((difference >= Constants.PATH.Path_Accuracy_Lower.doubleValue())
				&& (difference <= Constants.PATH.Path_Accuracy_Upper
						.doubleValue())) {
			shortestAngle(currentHeading, targetHeading, new MInteger(5));
		}
		// if the difference is large the system will enter this if statement
		// and adjust the rudder a lot
		else if (difference > Constants.PATH.Path_Accuracy_Upper.doubleValue()) {
			shortestAngle(currentHeading, targetHeading, new MInteger(15));
		}
		// calculate bearing
	}

	// This method is used to determine the bearing we should be on to reach the
	// next way point
	public double determineHeading() throws NoConnection, NoValue, OutOfRange {
		double longitude1 = currentPositionRead.getLongitude().getDMS()
				.doubleValue();
		double longitude2 = nextHeading.getLongitude().getDMS().doubleValue();
		double latitude1 = Math.toRadians(currentPositionRead.getLatitude()
				.getDMS().doubleValue());
		double latitude2 = Math.toRadians(nextHeading.getLatitude().getDMS()
				.doubleValue());
		double longDiff = Math.toRadians(longitude2 - longitude1);
		double y = Math.sin(longDiff) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2)
				* Math.cos(longDiff);

		return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

	}

	// this method is used to determine if we have arrived at the next
	// destination. This is calculated if the distance between our current
	// position and
	// the target waypoint is less than 10 meters
	private boolean arrived() throws NoConnection, NoValue, OutOfRange {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(nextHeading.getLatitude().getDMS()
				.doubleValue()
				- currentPositionRead.getLatitude().getDMS().doubleValue());
		double dLng = Math.toRadians(nextHeading.getLongitude().getDMS()
				.doubleValue()
				- currentPositionRead.getLongitude().getDMS().doubleValue());
		double a = Math.sin(dLat / 2)
				* Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(currentPositionRead.getLatitude()
						.getDMS().doubleValue()))
				* Math.cos(Math.toRadians(nextHeading.getLatitude().getDMS()
						.doubleValue())) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = (earthRadius * c);

		if (distance < 0.0621371192) // 10 meters in miles
		{
			logger.info("Waypoint reached");
			return true;
		} else {
			logger.info("Distance to Next Waypoint: {}", distance);
			return false;
		}
	}

	// This method is used in order to check if the end of the trip has been
	// reached, i.e there are no more way points in the list.
	private boolean endOfTrip() {
		if (count == wayPointList.size() - 1) {
			return true;
		} else {
			return false;
		}
	}

	public void ejbTimeout(Timer timer) {
		try {
			readPosition();
			boolean arrive = arrived();
			if (arrive && endOfTrip()) {
				// If we have arrived and its the end of the trip (no more way
				// points)
				logger.info("Arrived at destination");
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				motorController.stop();
				wayPointList.clear();
			} else if (arrive && !endOfTrip()) {
				// if we have arrived at our next way point but its not the end
				// of the trip
				logger.info(
						"Arrived at waypoint {}. Following path to waypoint {}",
						wayPointList.get(count), wayPointList.get(count + 1));
				count++;
				// we get the next way points from the list and drive.
				Latitude lat = new Latitude(new DegreesDecimal(wayPointList
						.get(count).getLat()));
				Longitude lng = new Longitude(new DegreesDecimal(wayPointList
						.get(count).getLng()));
				Coordinate coordinate = new Coordinate(lat, lng);
				setNextHeading(coordinate);
				drive();
			} else {
				// else if we are on our way to the next way point we continue
				// driving the vessel
				drive();
			}
		} catch (NoConnection e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoValue e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OutOfRange e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setCruisingThrust() throws NoConnection, InterruptedException,
			ConfigurationError, OutOfRange {
		motorController.stop(); // Stop motor
		motorController.increase(); // Set speed to 1
		motorController.increase(); // Set speed to 2
		motorController.increase(); // Set speed to 3
	}

	public void startFollowingPath() throws NoConnection, InterruptedException,
			ConfigurationError, OutOfRange {
		logger.info("Start following path");
		// POP OUT ITEM SOMEHOW
		count = 0;
		setCruisingThrust();
		if (routeReverse == true) {
			Collections.reverse(wayPointList);
			routeReverse = false;
		}
		// we set the next way point to the first in the list
		Latitude lat = new Latitude(new DegreesDecimal(wayPointList.get(count)
				.getLat()));
		Longitude lng = new Longitude(new DegreesDecimal(wayPointList
				.get(count).getLng()));
		Coordinate coordinate = new Coordinate(lat, lng);
		setNextHeading(coordinate);
		// we create the timer schedule for every 1 sec.
		if (timer != null) {
			timer.cancel();
		}
		timer = timerService.createTimer(0, 200, this.getClass().getName());
	}

	public void stopFollowingPath() throws NoConnection {
		logger.info("Stop following path");
		// Cancel the timer
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		motorController.stop();
		wayPointList.clear();
		path.clear();
	}

	public void returnHome() throws NoConnection, InterruptedException,
			ConfigurationError, OutOfRange {
		// setCruisingThrust();
		// timer.cancel();
		// setNextHeading(wayPointList.get(0).getCoordinate()); //we set the
		// next way point to the first in the list
		// timer.addSchedule(this,0,10);
	}

	public void reverseTheRoute() throws NoConnection, InterruptedException,
			ConfigurationError, OutOfRange {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		routeReverse = true;
	}
}
