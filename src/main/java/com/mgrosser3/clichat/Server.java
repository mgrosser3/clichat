package com.mgrosser3.clichat;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends Thread {

    public static ArrayList<String> getIPs() throws IOException {
        ArrayList<String> ips = new ArrayList<>();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {

            NetworkInterface i = (NetworkInterface) interfaces.nextElement();
            Enumeration<InetAddress> addresses = i.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress a = (InetAddress) addresses.nextElement();
                String regExIPv4 = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

                // Print valid ip addresses (ipv4)
                if (a.isReachable(400) && a.getHostAddress().matches(regExIPv4))
                    ips.add(a.getHostAddress());
            }
        }

        return ips;
    }

    /** Server socket handles the incoming connections. */
    private final ServerSocket socket;

    /** Server socket lock object */
    private final Lock socketLock = new ReentrantLock();

    /** Flag is necessary to leave the thread. */
    private final AtomicBoolean isDone = new AtomicBoolean(false);

    /** Flag to suppress all outputs */
    private final AtomicBoolean isSilent = new AtomicBoolean(false);

    /** List of all active connections. */
    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final Lock connectionsLock = new ReentrantLock();

    /**
     *  Initialized the ServerSocket object.
     *  @param port Port on which the server is listening.
     */

    public Server(int port) throws IOException {
        this.socket = new ServerSocket(port);
        System.out.printf("IP Addresses: %s\nPort: %d\n\n%n", Server.getIPs(), this.getLocalPort());
    }

    /** Get the port on which the server is listening. */
    public int getLocalPort()
    {
        int ret;

        this.socketLock.lock();
        try {
            ret = this.socket.getLocalPort();
        } finally {
            this.socketLock.unlock();
        }

        return ret;
    }

    public Boolean isDone() {
        return this.isDone.get();
    }

    public Integer getNumberOfConnections() {
        return this.connections.size();
    }

    public void setSilentMode(boolean value) {
        this.isSilent.set(value);
    }

    public Boolean isSilent() {
        return this.isSilent.get();
    }

    public void run() {

        if(!this.isSilent()) {
            System.out.println("Server is running ...");
        }

        // Server main loop ...

        while(!this.isDone.get()) {
            try {

                assert socket != null;

                if(!this.isSilent()) {
                    System.out.println("Waiting for new connetion ... ");
                }

                Connection connection = null;
                try {
                    connection = new Connection(socket.accept());

                } catch (SocketException e) {
                    if(!this.isDone.get()) {
                        System.err.println("Error: Server socket was terminated unexpectedly.");
                        this.isDone.set(true);
                    }
                    break;
                }

                // OnMessage event handler
                connection.addEventHandler((Object sender, String message) -> {

                    if(!this.isSilent()) {
                        System.out.println("Message received ... ");
                    }

                    this.connectionsLock.lock();
                    try {
                        for(Connection c : this.connections) {
                            if(!c.equals((Connection) sender)) {
                                c.send(message);
                            }
                        }
                    } finally {
                        this.connectionsLock.unlock();
                    }
                });

                // OnClose event handler
                connection.addEventHandler((Object sender) -> {
                    this.connectionsLock.lock();
                    try {
                        this.connections.remove((Connection) sender);
                    } finally {

                        if(!this.isSilent()) {
                            System.out.println("Connection removed ... ");
                        }

                        this.connectionsLock.unlock();
                    }
                });

                connection.start();

                this.connectionsLock.lock();
                this.connections.add(connection);
                this.connectionsLock.unlock();

                if(!this.isSilent()) {
                    System.out.println("New connection added ... ");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Server shut down ...

        this.socketLock.lock();
        try {
            assert socket != null;
            socket.close();
        } catch (IOException e) {
            System.err.println("Error: Server socket could not be closed correctly");
            System.err.println(e.getMessage());
        } finally {
            this.socketLock.unlock();
        }

    }

    /** Initiates the shutdown of the server. Please note that you may have
     *  to wait for the termination using '.join()'.
     */
    public void shutDown() {
        this.isDone.set(true);

        this.socketLock.lock();
        try {
            this.socket.close();
        }
        // Information to the user that something has gone wrong here.
        catch (IOException e) {
            System.err.println("Error: Server socket could not be closed correctly.");
            System.err.println(e.getMessage());
        } finally {
            this.socketLock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        int port = 5000;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        server.start();

        System.out.println("Press any key to shut down the server ...");
        new BufferedReader(new InputStreamReader(System.in)).readLine();

        server.shutDown();
        server.join();
    }

}
