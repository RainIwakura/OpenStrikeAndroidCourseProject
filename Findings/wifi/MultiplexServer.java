package com.example.wifiserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import android.os.Handler;

public class MultiplexServer extends Thread {

    int port;
    ByteBuffer readbuffer;
    Selector selector;
    ServerSocketChannel serverSock;
    Set<SelectionKey> readyKeys;
    private int numOfRequests = 0;
    private Queue<ClientInfo> activeRequests;
    private int numOfActiveRequests = 0;
    private Handler handle;
    
    public MultiplexServer(Integer port, Handler handle) {
        // process the command-line args
      

        this.port = port;
        this.handle = handle;
        
        //  This class gives us all our nio objects
        //	It's a single instance held by the JVM
        SelectorProvider sp = SelectorProvider.provider();

        // initialize main buffer
        readbuffer = ByteBuffer.allocate(1024);

        // Create the selector
        // create and bind the server socket
        try {
            selector = sp.openSelector();
            serverSock = sp.openServerSocketChannel();
            ServerSocket servSocket = serverSock.socket();
            servSocket.bind(new InetSocketAddress("localhost", port));
            serverSock.configureBlocking(false);
            serverSock.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException io) {
            System.out.println("server: cannot initialize socket.");
            System.exit(-1);
        }

        // Start waiting for requests
        run();

    }

    /*
     * Handle all the clients
     */
    @Override
    public void run() {
        int bytesRead;
        activeRequests = new LinkedList<ClientInfo>();
        boolean closeConnection = false;
        for (;;) {

            // Wait for something to happen on one of our sockets
            // If nothing is happening then we can feel free to block
            System.out.println("Waiting for something to happen.");
            try {
                selector.select();
            } catch (IOException ioe) {
                System.out.println("IOExcetion: line 126");
                ioe.printStackTrace();
            }
            // get the keys that have had some action and loop through them all
            System.out.println("OK. Something happened.");
            readyKeys = selector.selectedKeys();

            Iterator<SelectionKey> it = readyKeys.iterator();
            System.out.println("There are " + readyKeys.size() + " sockets ready to be checked.");

            // Wait so we can follow the output
            try {
                Thread.sleep(1000);

            } catch (InterruptedException ie) {
                System.out.println("Interrupted exception: line 138");
            }
            while (it.hasNext()) {
                // Two possibilities: it's a new client knocking
                // or it's an existing client with a request
                SelectionKey key = (SelectionKey) it.next();

                // a NEW CLIENT
                if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

                    System.out.println("Action on the listen socket.");
                    // So this socket is the server socket
                    // Could skip this step since there is only one
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    System.out.println("SOCKET " + ssc);

                    // You won't block here, because we are guaranteed it is ready
                    try {
                        SocketChannel sc = ssc.accept();
                        System.out.println("Accepted a new client.");
                        ClientInfo info = new ClientInfo(System.currentTimeMillis(), 0l, numOfRequests);

                        // Now add the new client sock to set of what is being listened to
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ, info);
                    } catch (IOException ioe) {
                        System.out.println("IOException: line 168");
                        ioe.printStackTrace();
                    }

                    System.out.println("Registered a new client.");
                } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    System.out.println("Action on a client socket.");
                    // So this socket is a client socket
                    SocketChannel sc = (SocketChannel) key.channel();
                    System.out.println("SOCKET " + sc);
                    // read a message from the client
                    bytesRead = 0;
                    readbuffer.clear();
                    boolean requestActive = false;
                    try {
                        bytesRead = sc.read(readbuffer);
                    } catch (IOException ioe) {
                        System.out.println("IOException: line 192");
                        ioe.printStackTrace();
                    }
                    System.out.println("Read " + bytesRead + " bytes from a client socket.");
                    String response = new String();
                    response = "";
                    ClientInfo info = (ClientInfo) sc.keyFor(selector).attachment();

                    if (bytesRead == -1) {
                        System.out.println("Client has disconnected.");
                        key.cancel();
                        try {
                            sc.close();
                        } catch (IOException ioe) {
                            System.out.println("IOException: line 212");
                            ioe.printStackTrace();
                        }
                    } else {
                        String request = new String(readbuffer.array(), 0, bytesRead);
                        System.out.println("The client said: " + request);

                        switch (request) {
                            case "ACTIVE": {
                                {
                                    if (this.activeRequests.isEmpty() != true) {
                                        Long time = System.currentTimeMillis();
                                        Iterator<ClientInfo> x = this.activeRequests.iterator();
                                        while (x.hasNext()) {
                                            if (-(x.next().getTimeOfLastRequest() - System.currentTimeMillis()) >= 60000) {
                                                this.numOfActiveRequests--;
                                                x.remove();
                                            }
                                        }
                                    }
                                }
                                requestActive = true;
                                break;
                            }
                            case "ON_SINCE":
                                response = "You've been connected for: "
                                        + (int) ((System.currentTimeMillis() - info.timeOfConnection) / 1000) + " s";
                                break;
                            case "IDLE_SINCE": {
                                response = "You've been idle for: "
                                        + (int) ((System.currentTimeMillis() - info.timeOfLastRequest) / 1000) + " s";
                                break;
                            }
                            case "REQUESTS":
                                response = "Number of requests served: " + numOfRequests;
                                break;
                            case "QUIT": {
                                response = "Closing connection";
                                closeConnection = true;
                                break;
                            }
                            default: {
                                response = "BAD";
                                break;
                            }
                        }
                        info.setTimeOfLastRequest(System.currentTimeMillis());

                        numOfRequests++;
                        boolean beenServed = false;

                        for (ClientInfo i : this.activeRequests) {
                            if (i.getId() == info.getId()) {
                                i.setTimeOfLastRequest(info.timeOfLastRequest);
                                beenServed = true;
                            }
                        }
                        if (!beenServed) {
                            activeRequests.add(info);
                            numOfActiveRequests++;
                        }
                        if (requestActive) {
                            response = "Number of active clients: " + numOfActiveRequests ;
                            requestActive = false;
                        }

                        // switch the buffer so the input is heading out
                        readbuffer.clear();
                        System.out.println(response);
                        // clear buffer from remaining chars 
                        response += "    ";
                        readbuffer.put(response.getBytes());
                        readbuffer.flip();
                        try {
                            sc.write(readbuffer);
                        } catch (IOException ioe) {
                            System.out.println("IOexception: line 239");
                            ioe.printStackTrace();
                        }

                        readbuffer.clear();
                      
                        if (closeConnection) {
         closeConnection = false;
                            System.out.println("Client has disconnected.");
                            key.cancel();
                            try {
                                sc.close();
                            } catch (IOException ioe) {
                                System.out.println("IOException: line 212");
                                ioe.printStackTrace();
                            }
                        }
                    }
                }
                // remove this key from the set of ready channels
                it.remove();

            }



        }
    }

   

    private class ClientInfo {

        private Long timeOfConnection;
        private Long timeOfLastRequest;
        private int id;

        public ClientInfo(Long connect, Long lrequest, int id) {
            timeOfConnection = connect;
            timeOfLastRequest = lrequest;
            this.id = id;
        }

        /**
         * @return the timeOfConnection
         */
        public Long getTimeOfConnection() {
            return timeOfConnection;
        }

        /**
         * @return the timeOfLastRequest
         */
        public Long getTimeOfLastRequest() {
            return timeOfLastRequest;
        }

        /**
         * @param timeOfLastRequest the timeOfLastRequest to set
         */
        public void setTimeOfLastRequest(Long timeOfLastRequest) {
            this.timeOfLastRequest = timeOfLastRequest;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }
    }
}