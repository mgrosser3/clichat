package tests;

import org.junit.Test;

import com.mgrosser3.clichat.Server;
import com.mgrosser3.clichat.Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClientTest {

    @Test
    public void ClientConnectsSendMessageAndLeaveTest() throws IOException, InterruptedException {

        final int port = 5008;
        Server server = new Server(port);
        server.start();
        Thread.sleep(1000);

        Client client = new Client("127.0.0.1", port);
        client.start();

        System.setIn(new ByteArrayInputStream("Hello World".getBytes()));

        Thread.sleep(1000);

        System.setIn(new ByteArrayInputStream("EXIT".getBytes()));

        assertTrue(client.isDone());

        client.join();

        server.shutDown();
        server.join();

    }

}
