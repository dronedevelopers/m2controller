public class servoTest {
	static final int ServoBase = 0; // positive ctrl input: topview, CCW
	static final int ServoShoulder = 1; // positive ctrl input: rightview, CCW
	static final int ServoElbow = 2; // positive ctrl input: rightview, CW
	static final int ServoPump = 3;
	static final int ServoValve = 4;

    public static void main(String[] args) throws InterruptedException {

		byte[] PWMctrlValsCurrent = new byte[CONST.RcPWMchanNum];
    	utilities util = new utilities(); 
		util.initilize();        
		
		M2StemController ctrller = new M2StemController();
		ctrller.connect(usrCfg.BleMACaddress);
        
        byte byPWMctrl = 0;
        while (util.isRunning()) {
	    	System.out.println("byPWMctrl:"+byPWMctrl);

    	    for (int CHii = 0; CHii < 6; CHii++) {
    	    	PWMctrlValsCurrent[CHii] = byPWMctrl;
    	    }
   	
	    	ctrller.setCtrl(PWMctrlValsCurrent,(byte)0);
            byte[] ctrlCmd = ctrller.getBinaryTxCtrlCmd();
            ctrller.writeCmd(ctrlCmd);
            util.delay_ms(30);
            byPWMctrl++;
        }
        System.out.println("exit\n");
        ctrller.disconnect();
    }

}
