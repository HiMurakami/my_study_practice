/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

/**
 * Oscilloscope demo application. Uses the demo sensor - change the
 * new DemoSensorC() instantiation if you want something else.
 *
 * See README.txt file in this directory for usage instructions.
 *
 * @author David Gay
 */

//nagamitsu
#include "TestFtsp.h"
#include "RadioCountToLeds.h"

configuration OscilloscopeAppC { }
implementation
{
  components OscilloscopeC, MainC, ActiveMessageC, LedsC, TimeSyncC,
    new TimerMilliC() as Timer, new TimerMilliC() as Timer1, new TimerMilliC() as Timer_one, new DemoSensorC() as Sensor;

//nagamitsu
    components new AMSenderC(AM_OSCILLOSCOPE);// as OcilloscopeSender;
    //mponents new AMReceiverC(AM_OSCILLOSCOPE);// as OcilloscopeReceiver;
    components new AMReceiverC(AM_RADIO_COUNT_MSG);// as OcilloscopeReceiver;
    //components new AMsenderC(AM_RADIO_COUNT_MSG) as FtspSender;
    //components new AMReceiverC([AM_TEST_FTSP_MSG) as FtspReceiver;

    //nagamitsu
    MainC.SoftwareInit -> TimeSyncC;
    TimeSyncC.Boot -> MainC;

    OscilloscopeC.Packet -> ActiveMessageC;
    OscilloscopeC.PacketTimeStamp -> ActiveMessageC;
    OscilloscopeC.GlobalTime -> TimeSyncC;
    OscilloscopeC.TimeSyncInfo -> TimeSyncC;
    //OscilloscopeC.AMSend -> ActiveMessageC.AMSend[AM_TEST_FTSP_MSG];
    //OscilloscopeC.Receive -> ActiveMessageC.Receive[AM_RADIO_COUNT_MSG];


  OscilloscopeC.Boot -> MainC;
  OscilloscopeC.RadioControl -> ActiveMessageC;
  /*OscilloscopeC.AMSend -> ActiveMessageC.AMSend;//OcilloscopeSender;
  OscilloscopeC.Recieve -> ActiveMessageC.Receive;//OcilloscopeReceiver;*/
    OscilloscopeC.AMSend -> AMSenderC;
  OscilloscopeC.Receive -> AMReceiverC;
  OscilloscopeC.Timer -> Timer;
  OscilloscopeC.Timer_one -> Timer_one;
    OscilloscopeC.Timer1 -> Timer1;
  //OscilloscopeC.Timer -> TimerMilliC;
  OscilloscopeC.Read -> Sensor;
  OscilloscopeC.Leds -> LedsC;

  
}
