#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# the spider walks following command from PC(ble direct connection) or internet(mqtt remote control)

from m2controller import m2controller
from m2controller import m2Const
import time
import usrCfg
import signal
import sys

requestExit = False
def signal_handler(sig, frame):
    global requestExit
    print('user Ctrl-C exit request')
    quit()
    
def callbackfuncCloud(telemetry):

    ########################################################################################
    # select one cartoon animation by assigning its string name to the variable
    ########################################################################################
    ledData = telemetry['m_baUartTx']
    ########################################################################################
    # give the choice of cartoon and frame to the SDK function as function input arguments 
    ########################################################################################
    controller.sendUart(ledData)
    ########################################################################################
    # optionally put other control command, such as servo or motor control in the same control command
    ########################################################################################
    #controller.motorHbridgeCtrl_pm1(m2Const.spiderPWMchSteer,-1.0)
    
    ########################################################################################
    # send out all the earlier configured settings, LED, servo, etc, to the hardware controller over cloud
    ########################################################################################
    controller.SendCmdTransBlking(False,False)
    #while not requestExit:
    time.sleep(1)

signal.signal(signal.SIGINT, signal_handler)

########################################################################################
# initialize the controller, the settings used by the initialization must be correctly configured beforehand
########################################################################################
controller = m2controller.BleCtrller(m2Const.etInternetBridgeToBle,None,usrCfg.BleMACaddress,callbackfuncCloud,0,usrCfg.InternetHostIPaddr,usrCfg.mqttPort,usrCfg.mqttusername,usrCfg.mqttpassword)
controller.connect()
time.sleep(1)
i=0
while True:
    if requestExit:
        break
    #controller.SendCmdTransBlking(False)
    time.sleep(1)
    #print(i)
    i+=1
########################################################################################
# Finally, do some housekeeping logic when we close the control link
########################################################################################
controller.stop()
sys.exit()

