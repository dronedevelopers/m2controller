/*
 * based on previous example, one movement action has been expanded to multiple actions.
 */
public class roboticArm3_multiGestures {
	static final int ServoBase = 0; // positive ctrl input: topview, CCW
	static final int ServoShoulder = 1; // positive ctrl input: rightview, CCW
	static final int ServoElbow = 2; // positive ctrl input: rightview, CW
	static final int ServoPump = 3;
	static final int ServoReleaseValve = 4;

    public static void main(String[] args) throws InterruptedException {

    	utilities util = new utilities(); 
		util.initilize();        
		
		M2StemController ctrller = new M2StemController();
		ctrller.connect(usrCfg.BleMACaddress);
        
    	byte[] PWMctrlValsCurrent = new byte[CONST.RcPWMchanNum];
    	byte[] PWMctrlValsTarget = new byte[CONST.RcPWMchanNum];
    	byte[] PWMctrlValsStepDistance = new byte[CONST.RcPWMchanNum]; // non-negative value
    	for (int ii = 0; ii < CONST.RcPWMchanNum; ii++) {
    		PWMctrlValsCurrent[ii] = 0;
    		PWMctrlValsTarget[ii] = 0;
    		PWMctrlValsStepDistance[ii] = 0;
    	}
    	byte u8GPIO_val = 0;
    	int iDanceStepii = 0;
    	boolean bLastStepReturn2Origin = false;
    	
    	// We bring the step size earlier here, because we will have multiple movement. Each needs definition of step size.
    	// Define here we can easily modify its value once, and change the behavior of all the subsequent operations
	    final byte stepSizeBase = 2;
	    final byte stepSizeJointSlow = 2;
	    final byte stepSizeJointFast = 4;
	    // In the final touch down moment, or the beginning of lifting moment, we may want to slow it down. Think about first contact.
	    final byte PrepareRange = 3;
	    final byte touchDownPositionShoulder = -67;
	    // depend on the sign of the touch down position, we + or - the Preparation Range
	    final byte touchDownPositionShoulderPrepare = (byte)(touchDownPositionShoulder+PrepareRange);
	    final byte touchDownPositionElbow = 67;
	    final byte touchDownPositionElbowPrepare = (byte)(touchDownPositionElbow-PrepareRange);
	    // here we introduce an elegant way to exit the while execution by hitting keyboard combination ctrl-C
	    while (util.isRunning()) {
	    	// switch is similar to a electrical switch, one connector at a time is connected to the outlet
    	    switch( iDanceStepii ) {
	    	    case 0: // robot arm ini gesture
	    	    	PWMctrlValsTarget[ServoBase] = 0;				PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;			PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;				PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoPump] = -127;			PWMctrlValsStepDistance[ServoPump] = 127;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = -127;	PWMctrlValsStepDistance[ServoReleaseValve] = 127;
	    	    	break;
	    	    case 1: // touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulderPrepare;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbowPrepare;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	break;
	    	    case 2: // final touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbow;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoPump] = 127;								PWMctrlValsStepDistance[ServoPump] = 127;
	    	    	break;
	    	    case 3: // vacuum pump 1sec operation
	    	    	util.delay_ms(900);
	    	    	break;
	    	    case 4: // move up slowly
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;		PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = (byte)(touchDownPositionElbow-10);	PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	break;
	    	    case 5: // move up
	    	    	PWMctrlValsTarget[ServoShoulder] = -20;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 20;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 6: // turn
	    	    	PWMctrlValsTarget[ServoBase] = 50;		PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	break;
	    	    case 7: // put it down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbow;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 8: // release
	    	    	PWMctrlValsTarget[ServoPump] = -127;		PWMctrlValsStepDistance[ServoPump] = 127;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = 127;	PWMctrlValsStepDistance[ServoReleaseValve] = 127;
	    	    	break;
	    	    case 9: // make sure release completed
	    	    	util.delay_ms(900);
	    	    	break;
	    	    case 10: // center
	    	    	PWMctrlValsTarget[ServoBase] = 0;			PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;		PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = -127;PWMctrlValsStepDistance[ServoReleaseValve] = 127;
	    	    	break;
	    	    case 11: // touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulderPrepare;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbowPrepare;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 12: // final touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbow;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoPump] = 127;							PWMctrlValsStepDistance[ServoPump] = 127;
	    	    	break;
	    	    case 13: // vacuum pump 1sec operation
	    	    	util.delay_ms(900);
	    	    	break;
	    	    case 14: // move up slowly
	    	    	PWMctrlValsTarget[ServoElbow] = (byte)(touchDownPositionElbow-10);	PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	break;
	    	    case 15: // move up
	    	    	PWMctrlValsTarget[ServoShoulder] = -20;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 20;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 16: // turn
	    	    	PWMctrlValsTarget[ServoBase] = -50;		PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	break;
	    	    case 17: // put it down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbow;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 18: // release
	    	    	PWMctrlValsTarget[ServoPump] = -127;			PWMctrlValsStepDistance[ServoPump] = 127;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = 127;		PWMctrlValsStepDistance[ServoReleaseValve] = 127;
	    	    	break;
	    	    case 19: // lift up
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 20: // return to ini state
	    	    	PWMctrlValsTarget[ServoBase] = 0;			PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;		PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoPump] = -127;		PWMctrlValsStepDistance[ServoPump] = 127;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = 127;	PWMctrlValsStepDistance[ServoReleaseValve] = 127;
		    		break;
	    		default:
	    			System.out.print("all operation completed\n");
	    			bLastStepReturn2Origin = true;
	    			break;
    	    }
    	    // once all steps have been completed, we can finish by exiting the while loop
	    	if (bLastStepReturn2Origin)
	    		break;
	    	
	    	// in previous example, we have only one goal. Here, we may multiple steps to run, 
	    	// hence we need to test whether the current goal has been reached. 
	    	// If all servos reach the destination position, current goal is reached.
    	    boolean bTargetReached = true;
    	    for (int ii = 0; ii < CONST.RcPWMchanNum; ii++) {
        		if (PWMctrlValsCurrent[ii] != PWMctrlValsTarget[ii]) {
        			bTargetReached = false;
        			break;
        		}
        	}
    	    // if the goal of current step has been reached, we need to run for the next step
    	    // key word "continue" skip the rest operation such as send command to robotic arm, wait for N milli-seconds, etc
    	    if (bTargetReached) {
    	    	iDanceStepii ++;
    	    	continue;
    	    }
    	    for (int CHii = 0; CHii < 5; CHii++) {
    	    	boolean Positive = true;
    	    	if (PWMctrlValsTarget[CHii] != PWMctrlValsCurrent[CHii]) {
    	    		if (PWMctrlValsTarget[CHii] < PWMctrlValsCurrent[CHii]) {
    	    			Positive = false;
    	    		}
    	    		if (Positive) {
    	    			if ((int)PWMctrlValsCurrent[CHii] + (int)PWMctrlValsStepDistance[CHii] > (int)PWMctrlValsTarget[CHii]) {
    	    				PWMctrlValsCurrent[CHii] = PWMctrlValsTarget[CHii];
    	    			}
    	    			else {
    	    				PWMctrlValsCurrent[CHii] += PWMctrlValsStepDistance[CHii];
    	    			}
    	    		}
    	    		else {
    	    			if ((int)PWMctrlValsCurrent[CHii] - (int)PWMctrlValsStepDistance[CHii] < (int)PWMctrlValsTarget[CHii]) {
    	    				PWMctrlValsCurrent[CHii] = PWMctrlValsTarget[CHii];
    	    			}
    	    			else {
    	    				PWMctrlValsCurrent[CHii] -= PWMctrlValsStepDistance[CHii];
    	    			}    	    			
    	    		}
    	    	}
    	    }
    	    System.out.print("step("+iDanceStepii+"): ");
	    	for (int ii = 0; ii < 5; ii++) {
	    		System.out.print(String.format("%d,", PWMctrlValsCurrent[ii]));
	    	}
	    	System.out.print("\n");
	    	
	    	ctrller.setCtrl(PWMctrlValsCurrent,u8GPIO_val);
	    	ctrller.writeCmd(ctrller.getBinaryTxCtrlCmd());
	    	util.delay_ms(100);
        }
        System.out.print("exit\n");
        ctrller.disconnect();
    }

}