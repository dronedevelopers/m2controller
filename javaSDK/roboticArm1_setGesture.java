/*
 * this is the starting point of robotic arm control, we simply set the target command control it will execute
 */
public class roboticArm1_setGesture {
	// here we first touch variable of built-in type: int
	// things like class, static, final, we will talk about them later, no worry now. Pay attention to the robotic action now
	// we have 5 actuators connected to the controller, defined here:
	static final int ServoBase = 0; 	// positive ctrl input: topview, 	CCW
	static final int ServoShoulder = 1; // positive ctrl input: rightview, 	CCW
	static final int ServoElbow = 2; 	// positive ctrl input: rightview, 	CW
	static final int ServoPump = 3;
	static final int ServoReleaseValve = 4;

    public static void main(String[] args) throws InterruptedException {
    	// do worry about these house keeping stuff now
    	utilities util = new utilities(); 
		util.initilize();        
		
		M2StemController ctrller = new M2StemController();
		// BleMACaddress is unique for each hardware controller. If not defined, it will never work
		ctrller.connect(usrCfg.BleMACaddress);
        
		// array is a collection of elements, which is of type byte here.
		// byte is in the range of -128 to 127, represented by 8 bit, or 1 byte
    	byte[] PWMctrlValsCurrent = new byte[CONST.RcPWMchanNum];
    	byte[] PWMctrlValsTarget = new byte[CONST.RcPWMchanNum];
    	byte[] PWMctrlValsStepDistance = new byte[CONST.RcPWMchanNum]; // non-negative value
    	// we want our variable to have known initial values
    	for (int ii = 0; ii < CONST.RcPWMchanNum; ii++) {
    		PWMctrlValsCurrent[ii] = 0;
    		PWMctrlValsTarget[ii] = 0;
    		PWMctrlValsStepDistance[ii] = 0;
    	}
    	byte u8GPIO_val = 0;
	    
    	// now we set the desired output for each servo channel
    	PWMctrlValsTarget[ServoBase] = 30;
    	PWMctrlValsTarget[ServoShoulder] = 20;
    	PWMctrlValsTarget[ServoElbow] = 0;
    	PWMctrlValsTarget[ServoPump] = -127;
    	PWMctrlValsTarget[ServoReleaseValve] = -127;
    	
    	// we set the control command to be sent out later
    	ctrller.setCtrl(PWMctrlValsTarget,u8GPIO_val);
    	// now the command is sent. 
    	// before it is sent, we can arbitrarily set desired command
        ctrller.writeCmd(ctrller.getBinaryTxCtrlCmd());
        util.delay_ms(1000);
        
        // print some message
        System.out.print("exit\n");
        
        // this is again house keeping staff
        ctrller.disconnect();
    }

}