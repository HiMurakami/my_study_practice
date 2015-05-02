import net.tinyos.message.*;
import net.tinyos.util.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class Oscilloscope implements MessageListener
{
    MoteIF mote;
    Data data;
    Window window;

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
	
//	for(int index_num=0;index_num<dataint.length;index_num++){
//	    System.out.println(omsg.get_id()+"\t"+dataint[index_num++]+"\t"+dataint[index_num]);
//	}
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
                        Date date1 = new Date();
                        SimpleDateFormat DF = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss.SSS");
                        ill1 = dataint;
			File file =new File("git1.txt");
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
	}
	else/* if(omsg.get_id()==3)*/{
	}
        try{
                Date date2 = new Date();
                SimpleDateFormat DF = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss.SSS");
		File file =new File("log/log_sensors_0913.txt");
		FileWriter filewriter=new FileWriter(file,true);
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
