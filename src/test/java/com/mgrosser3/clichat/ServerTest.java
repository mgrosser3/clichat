package tests;

import org.junit.Test;

import com.mgrosser3.clichat.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ServerTest {

    @Test
    public void ServerPortAlreadyUsed() throws InterruptedException {

        final int port = 5008;

        Server server1 = null;
        try {
            server1 = new Server(port);
            assertTrue(true);
        } catch (IOException e) {
            fail();
        }

        Thread.sleep(1000);

        // An error must occur when the socket is started.
        // As the port is already in use.
        Server server2 = null;
        try {
            server2 = new Server(port);
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        server1.shutDown();
        server1.join();

        // Since the first server is down, it should work now.
        try {
            server2 = new Server(port);
            assertTrue(true);
        } catch (IOException e) {
            fail();
        }

        server2.shutDown();
        server2.join();
    }

    @Test
    public void ConnectionsRegisterTest() throws IOException, InterruptedException {

        final int port = 5008;
        Server server = new Server(port);
        server.start();
        Thread.sleep(1000);

        int numberOfSockets = 5;
        ArrayList<Socket> sockets = new ArrayList<>();
        for(int i=0; i < numberOfSockets; i++) {
            sockets.add(new Socket("127.0.0.1", port));
            Thread.sleep(300);
        }

        assertEquals((Integer)numberOfSockets, server.getNumberOfConnections());

        for(Socket socket : sockets) {
            socket.close();
            Thread.sleep(300);
        }

        assertEquals((Integer)0, server.getNumberOfConnections());

        server.shutDown();
        server.join();
    }


    @Test
    public void ServerDistributeMessages() throws IOException, InterruptedException {

        final int port = 5008;
        Server server = new Server(port);
        server.start();
        Thread.sleep(3000);

        Socket client01 = new Socket("127.0.0.1", port);
        client01.setSoTimeout(5000);
        BufferedReader reader01 = new BufferedReader(new InputStreamReader(client01.getInputStream()));

        Socket client02 = new Socket("127.0.0.1", port);
        client02.setSoTimeout(5000);
        BufferedReader reader02 = new BufferedReader(new InputStreamReader(client02.getInputStream()));

        Socket client03 = new Socket("127.0.0.1", port);
        PrintWriter writer = new PrintWriter(client03.getOutputStream(), true);

        Thread.sleep(100);

        String exp_msg = "TEST MESSAGE";
        writer.println(exp_msg);

        assertEquals(exp_msg, reader01.readLine());
        assertEquals(exp_msg, reader02.readLine());

        server.shutDown();
        server.join();
    }
}
