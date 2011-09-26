/**
 * 
 */
package mise.demonstrator.control;

import mise.marssa.interfaces.control.IController;
import mise.marssa.interfaces.control.IRamping;
import mise.marssa.data_types.integer_datatypes.MInteger;
import mise.marssa.data_types.float_datatypes.MFloat;
import mise.marssa.exceptions.ConfigurationError;
import mise.marssa.exceptions.OutOfRange;

/**
 * @author Clayton Tabone
 *
 */
public class Ramping implements IRamping {
	int stepDelay;
	float currentValue, stepSize;
	IController controller;
	
	// true means positive ramping
	boolean direction = false;
	
	public Ramping(MInteger stepDelay, MFloat stepSize, IController controller) {
		this.stepDelay = stepDelay.getValue();
		this.stepSize = stepSize.getValue();
		this.controller = controller;
		this.currentValue = 0;
	}
	
	public Ramping(MInteger stepDelay, MFloat stepSize, IController controller, MFloat initialValue) {
		this.stepDelay = stepDelay.getValue();
		this.stepSize = stepSize.getValue();
		this.controller = controller;
		this.currentValue = initialValue.getValue();
	}
	
	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.electrical_motor_control.IRamping#rampTo(mise.marssa.data_types.float_datatypes.Percentage)
	 */
	@Override
	public void rampTo(MFloat desiredValue) throws InterruptedException, ConfigurationError, OutOfRange {
		float difference = desiredValue.getValue() - currentValue;
		direction = (difference > 0);
		while(true) {
			if(difference == 0) {
				// Do nothing. The desired value is the same as the current value.
			} else if(direction) {
				currentValue += stepSize;
			} else {
				currentValue -= stepSize;
			}
			controller.outputValue(new MFloat(currentValue));
            Thread.sleep(stepDelay);
            if((currentValue == desiredValue.getValue())) {
            	break;
            }
        }
	}

	@Override
	public MFloat getCurrentValue() {
		return new MFloat(currentValue);
	}
}
