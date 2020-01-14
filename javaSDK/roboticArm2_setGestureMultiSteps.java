/*
 * In the previous example, a large movement can be a very aggressive movement, since the servo tries to reach its destination in minimum time.
 * We break a large movement into a series of smaller movement in order to get a smooth action. 
 */
public class roboticArm2_setGestureMultiSteps {
	static final int ServoBase = 0; 	// positive ctrl input: topview, 	CCW
	static final int ServoShoulder = 1; // positive ctrl input: rightview, 	CCW
	static final int ServoElbow = 2; 	// positive ctrl input: rightview, 	CW
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
    	// introduce StepDistance, avoid sudden aggressive movement. Instead, many smaller steps
    	PWMctrlValsTarget[ServoBase] = 100;		PWMctrlValsStepDistance[ServoBase] = 2;
    	PWMctrlValsTarget[ServoShoulder] = 100;	PWMctrlValsStepDistance[ServoShoulder] = 2;
    	PWMctrlValsTarget[ServoElbow] = 100;		PWMctrlValsStepDistance[ServoElbow] = 2;
    	PWMctrlValsTarget[ServoPump] = -127;	PWMctrlValsStepDistance[ServoPump] = 127;
    	PWMctrlValsTarget[ServoReleaseValve] = -127;	PWMctrlValsStepDistance[ServoReleaseValve] = 127;

    	// keyword "while" continues run the code inside, until we explicitly tell it to stop, discussed in next episode
    	while (util.isRunning()) {
    		// computer program run so fast, we purposely slow it down. We will talk about more efficient multi-thread programming later
	    	util.delay_ms(100);
	    	    	
	    	// keyword "for" run its content for 5 times. variable CHii is of value 0 to 4 
    	    for (int CHii = 0; CHii < 5; CHii++) {
    	    	// boolean variable can be true or false
    	    	boolean Positive = true;
    	    	// test whether the current position has reached the destination position
    	    	if (PWMctrlValsTarget[CHii] != PWMctrlValsCurrent[CHii]) {
    	    		// since the step size is a length, determine here we are moving greater or smaller
    	    		if (PWMctrlValsTarget[CHii] < PWMctrlValsCurrent[CHii]) {
    	    			Positive = false;
    	    		}
    	    		// moving greater
    	    		if (Positive) {
    	    			// is this the last step which may not be of full step length? 
    	    			if ((int)PWMctrlValsCurrent[CHii] + (int)PWMctrlValsStepDistance[CHii] > (int)PWMctrlValsTarget[CHii]) {
    	    				PWMctrlValsCurrent[CHii] = PWMctrlValsTarget[CHii];
    	    			}
    	    			else {
    	    				PWMctrlValsCurrent[CHii] += PWMctrlValsStepDistance[CHii];
    	    			}
    	    		}
    	    		else { // moving smaller
    	    			// again, is this the last step which may not be of full step length? 
    	    			// why we cast the byte variable to int? It may over-flow
    	    			if ((int)PWMctrlValsCurrent[CHii] - (int)PWMctrlValsStepDistance[CHii] < (int)PWMctrlValsTarget[CHii]) {
    	    				PWMctrlValsCurrent[CHii] = PWMctrlValsTarget[CHii];
    	    			}
    	    			else {
    	    				PWMctrlValsCurrent[CHii] -= PWMctrlValsStepDistance[CHii];
    	    			}    	    			
    	    		}
    	    	}
    	    }
    	    // print the value of all the servo control command
	    	for (int ii = 0; ii < 5; ii++) {
	    		System.out.print(String.format("%d,", PWMctrlValsCurrent[ii]));
	    	}
	    	System.out.print("\n");
	    	
	    	ctrller.setCtrl(PWMctrlValsCurrent,u8GPIO_val);
	    	ctrller.writeCmd(ctrller.getBinaryTxCtrlCmd());
        }
        System.out.print("exit\n");
        ctrller.disconnect();
    }

}