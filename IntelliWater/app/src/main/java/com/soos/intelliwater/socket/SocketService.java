package com.soos.intelliwater.socket;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketService extends Service {

  private static final String TAG = SocketService.class.getName();

  public static String SERVER_IP;

  public static int SERVER_PORT;

  private final IBinder myBinder = new LocalBinder(this);

  private PrintWriter out;

  private Socket socket;

  private InetAddress serverAddress;

  public static void setServerPort(int port) {
    SERVER_PORT = port;
  }

  public static void setServerIp(String serverIp) {
    SERVER_IP = serverIp;
  }

  public Socket getSocket() {
    return socket;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  public void setOut(PrintWriter out) {
    this.out = out;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return myBinder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }


  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    Runnable connect = new ConnectSocket(this);
    new Thread(connect).start();
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      socket.close();
    } catch (Exception e) {
      Log.e(TAG, "[onDestroy] Cannot close socket connection: " + e);
    }
    socket = null;
  }

  public InetAddress getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(InetAddress serverAddress) {
    this.serverAddress = serverAddress;
  }

  public void IsBoundable() {
    Log.i(TAG, "[IsBoundable] server is boundalble. Ready to connect!");
  }

  public void setAdvancedMode(boolean isChecked) {
    if (out != null && !out.checkError()) {
      String message = "use_adv:" + isChecked;
      Log.e(TAG, "out message: " + message);
      AsyncTask.execute(() -> {
        out.println(message);
        out.flush();
      });
    } else {
      Log.w(TAG, "[setAdvancedMode] Cannot send message. No active connection!");
    }
  }

  public void setPumpStatus(boolean isChecked) {
    if (out != null && !out.checkError()) {
      String message = "pump:" + isChecked;
      Log.e(TAG, "out message: " + message);
      AsyncTask.execute(() -> {
        out.println(message);
        out.flush();
      });
    } else {
      Log.w(TAG, "[setPumpStatus] Cannot send message. No active connection!");
    }
  }
  public void setSollLevel(int level) {
    if (out != null && !out.checkError()) {
      String message = "soll:" + level;
      Log.e(TAG, "out message: " + message);
      AsyncTask.execute(() -> {
        out.println(message);
        out.flush();
      });
    } else {
      Log.w(TAG, "[setSollLevel] Cannot send message. No active connection!");
    }
  }
}