/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

import net.tinyos.message.*;
import net.tinyos.util.*;
import java.io.*;

/* The "Oscilloscope" demo app. Displays graphs showing data received from
   the Oscilloscope mote application, and allows the user to:
   - zoom in or out on the X axis
   - set the scale on the Y axis
   - change the sampling period
   - change the color of each mote's graph
   - clear all data

   This application is in three parts:
   - the Node and Data objects store data received from the motes and support
     simple queries
   - the Window and Graph and miscellaneous support objects implement the
     GUI and graph drawing
   - the Oscilloscope object talks to the motes and coordinates the other
     objects

   Synchronization is handled through the Oscilloscope object. Any operation
   that reads or writes the mote data must be synchronized on Oscilloscope.
   Note that the messageReceived method below is synchronized, so no further
   synchronization is needed when updating state based on received messages.
*/
public class Oscilloscope implements MessageListener
{
    MoteIF mote;
    Data data;
    Window window;

    //nagamitsu
    MoteIF mote_ftsp;
    PrintStream outReport = null;

    /* The current sampling period. If we receive a message from a mote
       with a newer version, we update our interval. If we receive a message
       with an older version, we broadcast a message with the current interval
       and version. If the user changes the interval, we increment the
       version and broadcast the new interval and version. */
    int interval = Constants.DEFAULT_INTERVAL;
    int version = -1;

    //nagamitsu
    /*    public class RunWhenShuttingDown extends Thread {
                public void run()
                {
                        System.out.println("Control-C caught. Shutting down...");
                        if (outReport!=null)
                        outReport.close();
                }
		}*/

    void connect()
        {
                try {
                        mote_ftsp = new MoteIF(PrintStreamMessenger.err);
                        mote_ftsp.registerListener(new TestFtspMsg(), this);
                        System.out.println("Connection ok!");
                }
                catch(Exception e) {
                        e.printStackTrace();
                        System.exit(2);
                }
        }

        public void writeReprot(TestFtspMsg tspr)
        {
                String foo = (System.currentTimeMillis()
                		+" "+tspr.get_src_addr()+" "+tspr.get_counter()
                        +" "+tspr.get_global_rx_timestamp()+" "+tspr.get_is_synced());
                outReport.println(foo);
                System.out.println(foo);
                outReport.flush();
        }

    public Oscilloscope(){
	
	//Runtime.getRuntime().addShutdownHook(new RunWhenShuttingDown());
	String name=""+System.currentTimeMillis();
	try{
	    outReport = new PrintStream(new FileOutputStream(name+".report"));
	    outReport.println("#[JAVA_TIME] [NODE_ID] [SEQ_NUM] [GLOB_TIME] [IS_TIME_VALID]");
                }
                catch (Exception e)
                {
                        System.out.println("FtspDataLogger.FtspDataLogger(): "+e.toString());
                }
    }


    /* Main entry point */
    void run() {
    data = new Data(this);
    window = new Window(this);
    window.setup();
    mote = new MoteIF(PrintStreamMessenger.err);
    mote.registerListener(new OscilloscopeMsg(), this);
    connect();
    }

    /* The data object has informed us that nodeId is a previously unknown
       mote. Update the GUI. */
    void newNode(int nodeId) {
    window.newNode(nodeId);
    }

    public synchronized void messageReceived(int dest_addr, 
            Message msg) {
    if (msg instanceof OscilloscopeMsg) {
        OscilloscopeMsg omsg = (OscilloscopeMsg)msg;

        /* Update interval and mote data */
        periodUpdate(omsg.get_version(), omsg.get_interval());
        data.update(omsg.get_id(), omsg.get_count(), omsg.get_readings());
	//aida
	int dataint[] =  omsg.get_readings();
	//nagamitsu shows dataint all
	for(int index_num=0;index_num<dataint.length;index_num++)
	System.out.println(dataint[index_num++] + "\t" + dataint[index_num]);


        /* Inform the GUI that new data showed up */
        window.newData();
    }
    //nagamitsu
    else if (msg instanceof TestFtspMsg){
                        //writeFullReprot((TestFtspMsg)msg);
                        writeReprot((TestFtspMsg)msg);
			System.out.println("FTSP mode");}
    }

    /* A potentially new version and interval has been received from the
       mote */
    void periodUpdate(int moteVersion, int moteInterval) {
    if (moteVersion > version) {
        /* It's new. Update our vision of the interval. */
        version = moteVersion;
        interval = moteInterval;
        window.updateSamplePeriod();
    }
    else if (moteVersion < version) {
        /* It's old. Update the mote's vision of the interval. */
        sendInterval();
    }
    }

    /* The user wants to set the interval to newPeriod. Refuse bogus values
       and return false, or accept the change, broadcast it, and return
       true */
    synchronized boolean setInterval(int newPeriod) {
    if (newPeriod < 1 || newPeriod > 65535) {
        return false;
    }
    interval = newPeriod;
    version++;
    sendInterval();
    return true;
    }

    /* Broadcast a version+interval message. */
    void sendInterval() {
    OscilloscopeMsg omsg = new OscilloscopeMsg();

    omsg.set_version(version);
    omsg.set_interval(interval);
    try {
        mote.send(MoteIF.TOS_BCAST_ADDR, omsg);
    }
    catch (IOException e) {
        window.error("Cannot send message to mote");
    }
    }

    /* User wants to clear all data. */
    void clear() {
    data = new Data(this);
    }

    public static void main(String[] args) {
    Oscilloscope me = new Oscilloscope();
    me.run();
    }
}
