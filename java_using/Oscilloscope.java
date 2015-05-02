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
import java.util.*;
import java.text.*;

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

    /* The current sampling period. If we receive a message from a mote
       with a newer version, we update our interval. If we receive a message
       with an older version, we broadcast a message with the current interval
       and version. If the user changes the interval, we increment the
       version and broadcast the new interval and version. */
    int interval = Constants.DEFAULT_INTERVAL;
    int version = -1;

    Calendar cal1 = Calendar.getInstance();
    public int[] ill1 = new int[10], ill2 = new int[10], ill3 = new int[10];

    /* Main entry point */
    void run() {
    data = new Data(this);
    window = new Window(this);
    window.setup();
    mote = new MoteIF(PrintStreamMessenger.err);
    mote.registerListener(new OscilloscopeMsg(), this);
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
	for(int index_num=0;index_num<dataint.length;index_num++){
	    System.out.println(omsg.get_id()+"\t"+dataint[index_num++]+"\t"+dataint[index_num]);
	}
	int year = cal1.get(Calendar.YEAR);
	int month = cal1.get(Calendar.MONTH) + 1;
	int day = cal1.get(Calendar.DATE);     
	int hour = cal1.get(Calendar.HOUR_OF_DAY);
	int minute = cal1.get(Calendar.MINUTE);
	int second = cal1.get(Calendar.SECOND);  

	if(omsg.get_id()==10){
		try{
			//File file0 = new File("sensor.txt");
        		//PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file0)));
        		//pw.print(dataint[0]+",");
        		//pw.close();
                        Date date1 = new Date();
                        SimpleDateFormat DF = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss.SSS");
                        ill1 = dataint;
			File file =new File("logNo1_0623_100ms_LEDbits4.txt");
			FileWriter filewriter=new FileWriter(file,true);
			//filewriter.write(year+","+month+","+day+","+hour+","+minute+","+second+","+dataint[0]+","+ill1+"\r\n");
			for(int i=0;i<dataint.length;i++){
			    filewriter.write(DF.format(date1)+","+dataint[i++]+","+dataint[i]+"\r\n");
			}
			filewriter.close();
		}
		catch (Exception e){
		}
	}

	else if(omsg.get_id()==20){
		try{
			//File file0 = new File("sensor.txt");
        		//PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file0,true)));
        		//pw.print(dataint[0]+",");
        		//pw.close();
		        Date date3 = new Date();
                        SimpleDateFormat DF = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss.SSS");
                        ill2 = dataint;
			File file =new File("logNo2_0623_100ms_bits.txt");
			FileWriter filewriter=new FileWriter(file,true);
			for(int i=0;i<dataint.length;i++){
			    filewriter.write(DF.format(date3)+","+dataint[i++]+","+dataint[i]+"\r\n");
			}
			filewriter.close();
		}
		catch (Exception e){
		}
	}

	else/* if(omsg.get_id()==3)*/{
		try{
			//File file0 = new File("sensor.txt");
        		//PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file0,true)));
        		//pw.print(dataint[0]);
        		//pw.close(); 
                        ill3 = dataint;
			File file =new File("logNo3_623.txt");
			FileWriter filewriter=new FileWriter(file,true);
			filewriter.write(year+","+month+","+day+","+hour+","+minute+","+second+","+dataint[0]+","+ill3+"\r\n");
			filewriter.close();
		}
		catch (Exception e){
		}
	}
        try{
	    //File file1 = new File("log/sensor.txt");
	    //PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file1)));
	        //pw.print(ill1+","+ill2+","+ill3);
		//pw.print(dataint[0] + "," + dataint[1] + "," + dataint[2]);
                //pw.close();
                Date date2 = new Date();
                SimpleDateFormat DF = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss.SSS");
		File file =new File("log/log_sensors_0913.txt");
		FileWriter filewriter=new FileWriter(file,true);
		//filewriter.write(year+","+month+","+day+","+hour+","+minute+","+second+","+dataint[0]+","+ill1+"\r\n");
		//filewriter.write(DF.format(date2)+","+dataint[0]+","+dataint[1]+","+dataint[2]+","+ill1+","+ill2+","+ill3+"\r\n");
		for(int i = 0;i <= dataint.length;i++){
		    filewriter.write(DF.format(date2)+","+dataint[i]+","+dataint[i+1]+","+ill1[i]+","+ill1[i+1]+","+ill2[i]+","+ill2[i+1]+","+ill3[i++]+","+ill3[i]+"\r\n");
		}
		filewriter.close();
        }
        catch (Exception e){
        }
        /* Inform the GUI that new data showed up */
        window.newData();
    }
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
    /*try {
	//nagamitsu memo
	//basestation sends a packet to motes
        mote.send(MoteIF.TOS_BCAST_ADDR, omsg);
    }
    catch (IOException e) {
        window.error("Cannot send message to mote");
	}*/
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
