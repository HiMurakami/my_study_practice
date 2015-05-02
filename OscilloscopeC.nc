#include "Timer.h"
#include "Oscilloscope.h"
#include "TestFtsp.h"
#include "RadioCountToLeds.h"

module OscilloscopeC @safe(){
	uses {
		interface Boot;
		interface SplitControl as RadioControl;
		interface AMSend;// as OcilloscopeSender;
		interface Receive;// as OcilloscopeReceiver;
		interface Timer<TMilli> as Timer;
		interface Read<uint16_t>;
		interface Leds;
		interface GlobalTime<TMilli>;
		interface TimeSyncInfo;
		interface Packet;
		interface PacketTimeStamp<TMilli,uint32_t>;
		interface Timer<TMilli> as Timer1;
		interface Timer<TMilli> as Timer_one;
	}
}
implementation{
	message_t sendBuf;
	bool sendBusy;
	message_t msgBuffer;
	message_t msgBuf;
	bool time_flag = FALSE;
	oscilloscope_t local;

	uint8_t reading; /* 0 to NREADINGS */
	uint16_t data_tmp; /* To compare datas */
	uint16_t data_change_tmp; /* To compare present data */
	uint8_t error_range; /* error data range */
	uint32_t globaltime; /* globaltime */
	uint8_t timer_count; /* timer intarval */
	uint32_t timer; /* timer */
	uint16_t data_[20]; /*To compare datas*/
	uint16_t illTmp = 0; //the ill when differ Max
//	uint16_t countBit = 0;
	bool suppressCountChange;

	void report_problem() { call Leds.led0Toggle(); }
	void report_sent() { call Leds.led1Toggle(); }
	void report_received() { call Leds.led2Toggle(); }

	void push_data(uint16_t ill){
		int i=0;
		for(i = 18;i>=0;i--){
			data_[i+1] = data_[i];
		}
		data_[0] = ill;
	}
	event void Boot.booted() {
		local.interval = DEFAULT_INTERVAL;
		local.id = TOS_NODE_ID;
		if (call RadioControl.start() != SUCCESS)report_problem();
	}
	void startTimer() {
		call Timer.startPeriodic(local.interval);
		reading = 0;
		call Timer1.startPeriodic(1);
		data_tmp = 0;
		data_change_tmp = 0;
		error_range = 4;
		timer_count = 0;
		timer = 0;
		globaltime = 0;
		for(timer_count = 0;timer_count < 20;timer_count++) data_[timer_count] = 0;
		timer_count=0;
	}
	event void RadioControl.startDone(error_t error){ startTimer(); }
	event void RadioControl.stopDone(error_t error) {}
	event message_t* Receive.receive(message_t* msg, void* payload, uint8_t len){
		call Leds.led2Toggle();
		if (!sendBusy && call PacketTimeStamp.isValid(msg)) {
			radio_count_msg_t* rcm = (radio_count_msg_t*)call Packet.getPayload(msg, sizeof(radio_count_msg_t));
			test_ftsp_msg_t* report = (test_ftsp_msg_t*)call Packet.getPayload(&msgBuf, sizeof(test_ftsp_msg_t));

			uint32_t rxTimestamp = call PacketTimeStamp.timestamp(msg);

           		report->src_addr = TOS_NODE_ID;
          		report->counter = rcm->counter;
         		report->local_rx_timestamp = rxTimestamp;
           		report->is_synced = call GlobalTime.local2Global(&rxTimestamp);
           		report->global_rx_timestamp = rxTimestamp;
         		report->skew_times_1000000 = (uint32_t)call TimeSyncInfo.getSkew()*1000000UL;
			report->ftsp_root_addr = call TimeSyncInfo.getRootID();
			report->ftsp_seq = call TimeSyncInfo.getSeqNum();
        		report->ftsp_table_entries = call TimeSyncInfo.getNumEntries();
	    
			globaltime = report->global_rx_timestamp;

			if(len == sizeof(radio_count_msg_t)) call Leds.led0Toggle();

			if(!time_flag && report->is_synced == 0 && TOS_NODE_ID == 10){
				call Timer_one.startOneShot(30);
				time_flag = TRUE;
				local.readings[0] = report->is_synced;
			}
		}
		return msg;
	}

	event void Timer.fired() {
		if (reading == NREADINGS){
			if (!sendBusy && sizeof local <= call /*OcilloscopeSender*/AMSend.maxPayloadLength()){
				memcpy(call /*OcilloscopeSender*/AMSend.getPayload(&sendBuf, sizeof(local)), &local, sizeof local);
				if (call /*OcilloscopeSender*/AMSend.send(AM_BROADCAST_ADDR, &sendBuf, sizeof local) == SUCCESS){
					sendBusy = TRUE;
				}
			}
			if (!sendBusy) report_problem();
			reading = 0;
			if (!suppressCountChange) local.count++;
			suppressCountChange = FALSE; 
     		}
		if (call Read.read() != SUCCESS) report_problem();
	}

	event void Timer1.fired(){ globaltime++; }
	event void Timer_one.fired(){ call Timer.startPeriodic(local.interval); }
	event void AMSend.sendDone(message_t* msg, error_t error) {
		if (error == SUCCESS) report_sent();
		else                  report_problem();
		sendBusy = FALSE;
	}

	event void Read.readDone(error_t result, uint16_t data) {
		if (result != SUCCESS){
			data = 0xffff;
			report_problem();
		}
		if (reading < NREADINGS){
			local.readings[reading++] = data;//globaltime;
			local.readings[reading++] = globaltime;
      			push_data(data);
      		}

	}
}
