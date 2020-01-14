public class roboticArm4_binaryOp {
	static final int ServoBase = 0; // positive ctrl input: topview, CCW
	static final int ServoShoulder = 1; // positive ctrl input: rightview, CCW
	static final int ServoElbow = 2; // positive ctrl input: rightview, CW
	static final int ServoPump = 3;
	static final int ServoReleaseValve = 4;
	// valid input range of servo command is -127 to 127, set the servo to move from one end to the other end
	// the value -128 is a reserved value indicating the command should be set to its end target value without any intermediate step/steps
	static final byte byRunMaxSpd = -128;
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
    	
	    final byte stepSizeBase = 2;
	    final byte stepSizeJointSlow = 2;
	    final byte stepSizeJointFast = 4;
	    final byte PrepareRange = 3;
	    final byte touchDownPositionShoulder = -67;
	    final byte touchDownPositionShoulderPrepare = (byte)(touchDownPositionShoulder+PrepareRange);
	    final byte touchDownPositionElbow = 67;
	    final byte touchDownPositionElbowPrepare = (byte)(touchDownPositionElbow-PrepareRange);

	    while (util.isRunning()) {
    	    switch( iDanceStepii ) {
	    	    case 0: // robot arm ini gesture
	    	    	PWMctrlValsTarget[ServoBase] = 0;				PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;			PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;				PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoPump] = -127;			PWMctrlValsStepDistance[ServoPump] = byRunMaxSpd;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = -127;	PWMctrlValsStepDistance[ServoReleaseValve] = byRunMaxSpd;
	    	    	break;
	    	    case 1: // touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulderPrepare;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbowPrepare;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	break;
	    	    case 2: // final touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbow;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoPump] = 127;								PWMctrlValsStepDistance[ServoPump] = byRunMaxSpd;
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
	    	    	PWMctrlValsTarget[ServoPump] = -127;		PWMctrlValsStepDistance[ServoPump] = byRunMaxSpd;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = 127;	PWMctrlValsStepDistance[ServoReleaseValve] = byRunMaxSpd;
	    	    	break;
	    	    case 9: // make sure release completed
	    	    	util.delay_ms(900);
	    	    	break;
	    	    case 10: // center
	    	    	PWMctrlValsTarget[ServoBase] = 0;			PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;		PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = -127;PWMctrlValsStepDistance[ServoReleaseValve] = byRunMaxSpd;
	    	    	break;
	    	    case 11: // touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulderPrepare;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbowPrepare;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 12: // final touch down
	    	    	PWMctrlValsTarget[ServoShoulder] = touchDownPositionShoulder;PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoElbow] = touchDownPositionElbow;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointSlow;
	    	    	PWMctrlValsTarget[ServoPump] = 127;							PWMctrlValsStepDistance[ServoPump] = byRunMaxSpd;
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
	    	    	PWMctrlValsTarget[ServoPump] = -127;			PWMctrlValsStepDistance[ServoPump] = byRunMaxSpd;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = 127;		PWMctrlValsStepDistance[ServoReleaseValve] = byRunMaxSpd;
	    	    	break;
	    	    case 19: // lift up
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;	PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;		PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	break;
	    	    case 20: // return to ini state
	    	    	PWMctrlValsTarget[ServoBase] = 0;			PWMctrlValsStepDistance[ServoBase] = stepSizeBase;
	    	    	PWMctrlValsTarget[ServoShoulder] = 0;		PWMctrlValsStepDistance[ServoShoulder] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoElbow] = 0;			PWMctrlValsStepDistance[ServoElbow] = stepSizeJointFast;
	    	    	PWMctrlValsTarget[ServoPump] = -127;		PWMctrlValsStepDistance[ServoPump] = byRunMaxSpd;
	    	    	PWMctrlValsTarget[ServoReleaseValve] = 127;	PWMctrlValsStepDistance[ServoReleaseValve] = byRunMaxSpd;
		    		break;
	    		default:
	    			System.out.print("all operation completed\n");
	    			bLastStepReturn2Origin = true;
	    			break;
    	    }
	    	if (bLastStepReturn2Origin)
	    		break;
	    	
    	    boolean bTargetReached = true;
    	    for (int ii = 0; ii < CONST.RcPWMchanNum; ii++) {
        		if (PWMctrlValsCurrent[ii] != PWMctrlValsTarget[ii]) {
        			bTargetReached = false;
        			break;
        		}
        	}
    	    if (bTargetReached) {
    	    	iDanceStepii ++;
    	    	continue;
    	    }
    	    for (int CHii = 0; CHii < 5; CHii++) {
    	    	// here we first check whether the speed setting is the reserved no intermediate mode
    	    	if (PWMctrlValsStepDistance[CHii] == byRunMaxSpd) {
    	    		PWMctrlValsCurrent[CHii] = PWMctrlValsTarget[CHii];
    	    	}
    	    	else {
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