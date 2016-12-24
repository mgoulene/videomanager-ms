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

        long timeToFree = System.currentTimeMillis() - period;
        while (calls.size() != 0 && calls.getFirst().longValue() < timeToFree) {
            log.debug(System.identityHashCode(this) + " : Removing one of "+calls.size());
            calls.removeFirst();
        }

    }

    public synchronized void waitForCall() throws RuntimeException {
        log.debug(System.identityHashCode(this) + " : Size is : "+calls.size());
        clean();
        long currentTimeMillis = System.currentTimeMillis();
        while (calls.size() >= callsPerPeriod) {
            clean();
            long timeToSleep = period - (currentTimeMillis - calls.getFirst().longValue());
            log.debug(System.identityHashCode(this) + " : Stack Full ("+calls.size()+"). Sleeping for "+timeToSleep+" ms");
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
        log.debug(System.identityHashCode(this) + " : Adding new. Size is : "+calls.size());
    }
}
