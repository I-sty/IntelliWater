package com.soos.intelliwater.socket;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

class ConnectSocket implements Runnable {

    private static final String TAG = ConnectSocket.class.getName();
    private SocketService socketService;

    ConnectSocket(SocketService socketService) {
        this.socketService = socketService;
    }

    @Override
    public void run() {
        try {
            socketService.setServerAddress(InetAddress.getByName(SocketService.SERVER_IP));
            Log.e(TAG, "C: Connecting...");

            //create a socket to make the connection with the server
            socketService.setSocket(new Socket(socketService.getServerAddress(), SocketService.SERVER_PORT));

            try {
                //send the message to the server
                socketService.setOut(new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketService.getSocket().getOutputStream())), true));
            } catch (Exception e) {
                Log.e(TAG, "S: Error", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
        }
    }
}
