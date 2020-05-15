package com.bingo.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

public class ContentIdGenerator {

    static int seq = 0;
    
    static String hostname;

    public static void getHostname() {
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            // we can't find our hostname? okay, use something no one else is
            // likely to use
            hostname = new Random(System.currentTimeMillis()).nextInt(100000) + ".localhost";
        }
    }

    /**
     * Sequence goes from 0 to 100K, then starts up at 0 again. This is large
     * enough,
     * and saves
     * 
     * @return
     */
    public static synchronized int getSeq() {
        return (seq++) % 100000;
    }

    /**
     * One possible way to generate very-likely-unique content IDs.
     * 
     * @return A content id that uses the hostname, the current time, and a
     *         sequence number
     *         to avoid collision.
     */
    public static String getContentId() {
        getHostname();
        int c = getSeq();
        return c + "." + System.currentTimeMillis() + "@" + hostname;
    }
}
