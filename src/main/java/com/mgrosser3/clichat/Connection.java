package com.mgrosser3.clichat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection extends Thread {

    @FunctionalInterface
    public interface OnMessageEventHandler {
        void onMessage(Object sender, String message);
    }

    @FunctionalInterface
    public interface OnDisconnectEventHandler {
        void onDisconnect(Object sender);
    }

    /**
     * Connection socket for input and output streaming.
     */
    private final Socket socket;

    /**
     * Used for synchronized access to the socket object.
     */
    private final Lock socketLock = new ReentrantLock();

    /**
     * PrintWriter for output streaming.
     */
    private final PrintWriter output;

    /**
     * List of all registered OnMessageEventHandler
     */
    private final List<OnMessageEventHandler> onMessageEventHandlers =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * Used for synchronized access to the onDisconnectEventHandlers object.
     */
    private final Lock messageEventHandlerLock = new ReentrantLock();

    /**
     * List of all registered OnConnectionEventHandler
     */
    private final List<OnDisconnectEventHandler> onDisconnectEventHandlers =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * Used for synchronized access to the onDisconnectEventHandlers object.
     */
    private final Lock diconnectEventHandlerLock = new ReentrantLock();

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    public void addEventHandler(OnDisconnectEventHandler handler) {
        this.diconnectEventHandlerLock.lock();
        try {
            this.onDisconnectEventHandlers.add(handler);
        }finally {
            this.diconnectEventHandlerLock.unlock();
        }
    }

    public void addEventHandler(OnMessageEventHandler handler) {
        this.messageEventHandlerLock.lock();
        try {
            this.onMessageEventHandlers.add(handler);
        }finally {
            this.messageEventHandlerLock.unlock();
        }
    }

    public void send(String message) {
        this.output.println(message);
    }

    public void run() {

        BufferedReader input = null;

        this.socketLock.lock();
        try {
            // BufferedReader for input streaming.
            input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        } catch (IOException e) {
            System.err.println("Connection socket input stream could "+
                    "not be initialized correctly.");
            this.close();
        } finally {
            this.socketLock.unlock();
        }

        String message = "";
        while (!socket.isClosed()) {

            if (input == null) {
                this.close();
                break;
            }

            try {
                message = input.readLine();

                // EVENT: OnDisconnect
                this.messageEventHandlerLock.lock();
                try {
                    for (OnMessageEventHandler handler : this.onMessageEventHandlers) {
                        handler.onMessage(this, message);
                    }
                } finally {
                    this.messageEventHandlerLock.unlock();
                }


            } catch (IOException e) {
                System.err.println("Connection socket has been closed due to " +
                        "an error in the input stream.");
                this.close();
                break;
            }

            if (message == null) {
                this.close();
                break;
            }
        }

        // EVENT: OnDisconnect
        this.diconnectEventHandlerLock.lock();
        try {
            for (OnDisconnectEventHandler handler : this.onDisconnectEventHandlers) {
                handler.onDisconnect(this);
            }
        } finally {
            this.diconnectEventHandlerLock.unlock();
        }
    }

    public void close() {
        socketLock.lock();
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error: Connection socket could not be closed correctly.");
        } finally {
            socketLock.unlock();
        }
    }

}
