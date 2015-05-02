COMPONENT=OscilloscopeAppC

PFLAGS += -DTIMESYNC_RATE=3
#PFLAGS += -DTIMESYNC_DEBUG

#PFLAGS += -I$(TOSDIR)/lib/ftsp -I$(TOSDIR)/../apps/RadioCountToLeds
PFLAGS += -I$(TOSDIR)/lib/ftsp -I$(TOSDIR)/../apps/tests/TestFtsp/Ftsp -I$(TOSDIR)/../apps/RadioCountToLeds

include $(MAKERULES)
