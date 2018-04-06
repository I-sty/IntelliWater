package com.soos.intelliwater.socket;

import android.os.Binder;

public class LocalBinder extends Binder {
    private SocketService socketService;

    LocalBinder(SocketService socketService) {
        this.socketService = socketService;
    }

    public SocketService getService() {
        return socketService;
    }
}
