package com.mgrosser3.clichat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client extends Thread {

    /** Connection of the client. */
    private final Socket socket;

    /** Server socket lock object */
    private final Lock socketLock = new ReentrantLock();

    /** Flag is necessary to leave the thread. */
    private final AtomicBoolean isDone = new AtomicBoolean(false);

    public Client(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
    }

    public Boolean isDone() {
        return this.isDone.get();
    }

    private Thread getReceiver() {
        Thread receiver = new Thread(() -> {

            BufferedReader socketInput = null;
            socketLock.lock();
            try {
                socketInput = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                this.isDone.set(true);
                System.out.println("Error: Input stream could not be initialized.");
                System.out.println(e.getMessage());
            } finally {
                socketLock.unlock();
            }

            String receivedMsg = "";
            while(!this.isDone.get()) {

                try {
                    receivedMsg = socketInput.readLine();
                } catch (IOException e) {
                    if(!this.isDone.get()) {
                        this.isDone.set(true);
                        System.out.println("Server disconnected.\nClose the program with EXIT.");
                    }
                    break;
                }

                if(receivedMsg == null) {
                    this.isDone.set(true);
                    break;
                }

                if(receivedMsg.equals("null")) {
                    continue;
                }

                System.out.println("\033[1;38;5;46mother: \033[0m" + receivedMsg);
            }
        });

        return receiver;
    }

    public void run() {

        // Initialize the output stream and user input stream ...

        PrintWriter output = null;
        socketLock.lock();
        try {
            output = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error: Access to the socket output stream failed.");
            this.isDone.set(true);
        } finally {
            socketLock.unlock();
        }

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        // Initialize message receiver ...

        Thread receiver = getReceiver();
        receiver.start();

        // Client main loop ...

        String message = "";
        while(!this.isDone.get()) {

            try {
                message = userInput.readLine();
            } catch (IOException e) {
                System.err.println("Error: Client socket was terminated unexpectedly.");
                this.isDone.set(true);
                break;
            }

            if(message == null) {
                this.isDone.set(true);
                break;
            }

            if(message.equals("EXIT")) {
                this.isDone.set(true);
                break;
            }

            assert output != null;
            output.println(message);
        }

        socketLock.lock();
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            socketLock.unlock();
        }

        try {
            receiver.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *      Entry point for our client application
     *
     */
    public static void main(String[] args) throws InterruptedException, IOException {

        // Usage information and help
        String helpString = "Usage: java -cp out com.mgrosser3.sqlchat.Client " +
                "[--other_instance <IP>:<PORT>]";

        //
        //		Parse all command line arguments.
        //

        boolean isServer = true;
        String ip = "127.0.0.1";
        int port = 5001;

				ArrayList<String> unknownArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--other_instance")) {
                try {
										i++;
                    String[] address = args[i].split(":");
                    ip = address[0];
                    port = Integer.parseInt(address[1]);
                    isServer = false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error: Value of parameter --other_instances is missing or invalid.");
                    System.out.println(helpString);
										System.exit(1);
                } catch (NumberFormatException e) {
                    System.err.println("Error: Port should be a number.");
                    System.out.println(helpString);
										System.exit(1);
                }
            } else if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println(helpString);
								System.exit(0); 
						}

						else {
							unknownArgs.add(arg);	
						}
        }

				if(unknownArgs.size() > 0) {
					System.out.print("\nUnknown Arguments are ignored: ");
					System.out.println(unknownArgs);
					System.out.println(helpString);
					System.exit(1);
				}

        //
        //      Cleans the screen and prints the title.
        //

        String title =
                "********************************************************\n" +
                "* CLI Chat v0.1.0\n" +
                "* Copyright (C) Martin Großer\n" +
                "********************************************************\n";

        Terminal.clearScreen();
        System.out.println(title);
				
        //
        //      Starts the server, if necessary.
        //

        Server server = null;

        if (isServer) {
            server = new Server(port);
            server.setSilentMode(true);
            server.start();
            Thread.sleep(2000);
        }

				Client client = null;
				try{
					client = new Client(ip, port);
				} catch(IOException e) {
					System.out.println("Error: Connection to server failed.");
					System.exit(1);
				}

        client.start();
        client.join();

        // Shut down server
        if (isServer) {
            server.shutDown();
            server.join();
        }

        System.exit(0);
    }
}
