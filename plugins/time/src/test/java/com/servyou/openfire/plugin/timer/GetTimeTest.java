package com.servyou.openfire.plugin.timer;

import static org.junit.Assert.assertTrue;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetTimeTest {

    private static XMPPConnection connection;

    private static String serverIp = "192.168.70.106";

    private static int port = 5332;

    public static String username = "songy";

    public static String psd = "123";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testLogin() {
        if (connect() && login(username, psd)) {
            sengTimeIQ();
            assertTrue(true);
        } else {
            assertTrue(false); 
        }

    }

    public boolean connect() {
        XMPPConnection.DEBUG_ENABLED = true;
        connection = new XMPPConnection(new ConnectionConfiguration(serverIp, port));
        try {
            connection.connect();
            return true;
        } catch (XMPPException e) {
            // e.printStackTrace();
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try {
            connection.login(username, password);
        } catch (XMPPException e) {
            // e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void sengTimeIQ() {
        TimeIQ iq = new TimeIQ();
        iq.setFrom(connection.getUser());
        iq.setType(IQ.Type.GET);
        connection.sendPacket(iq);
    }

}
