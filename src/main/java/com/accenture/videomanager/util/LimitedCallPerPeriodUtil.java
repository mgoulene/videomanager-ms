package com.accenture.videomanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Created by vagrant on 12/11/16.
 */
public class LimitedCallPerPeriodUtil {

    private final Logger log = LoggerFactory.getLogger(LimitedCallPerPeriodUtil.class);


    private long period;
    private int callsPerPeriod;
    private LinkedList<Long> calls;

    public LimitedCallPerPeriodUtil(long _period, int _callsPerPeriod) {
        period = _period;
        callsPerPeriod = _callsPerPeriod;
        calls = new LinkedList<Long>();
    }

    private synchronized void clean() {
        log.debug("Try to clean Stack. Size is "+calls.size()+"/"+callsPerPeriod);
        long timeToFree = System.currentTimeMillis() - period;
        while (calls.size() != 0 && calls.getFirst().longValue() < timeToFree) {
            //log.debug("Removing one Element");
            calls.removeFirst();
        }

    }

    public synchronized void waitForCall() throws RuntimeException {
        clean();
        long currentTimeMillis = System.currentTimeMillis();
        if (calls.size() >= callsPerPeriod) {
            long timeToSleep = period - (currentTimeMillis - calls.getFirst().longValue());
            log.debug("Stack Full. Sleeping for "+timeToSleep+" ms");
            try {
                if (timeToSleep >0) {
                    Thread.sleep(timeToSleep);
                }
                //waitForCall();
            } catch (InterruptedException e) {
                throw  new RuntimeException(e);
            }
        }
        calls.addLast(new Long(currentTimeMillis));
    }
}
