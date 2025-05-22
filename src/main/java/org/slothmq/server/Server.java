package org.slothmq.server;

public class Server {
    //TODO the socket server should be a thread of is own, in order to not block any other connections.
    public static void main(String[] args) {
        new SlothHttpServer().start();
        new SlothSocketServer().start();
        //TODO use NIO for non-blocking communication
    }
}
