package net.bidd.car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarControl {
    private DatagramSocket datagramSocket = null;
    private ExecutorService mThreadPool;
    private static CarControl carControl;

    public static CarControl getCarControl() {
        if (carControl == null) {
            synchronized (CarControl.class) {
                if (carControl == null) {
                    carControl = new CarControl();
                }
            }
        }
        return carControl;
    }

    private CarControl() {
        super();
        int cpuNumbers = Runtime.getRuntime().availableProcessors();
//        根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * 5);
    }

    public void write(final String msg) {
        try {
            if (null == datagramSocket) {
                datagramSocket = new DatagramSocket(Constants.SOCKET_UDP_PORT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                InetAddress targetAddress = null;
                try {
                    targetAddress = InetAddress.getByName(Constants.SOCKET_HOST);
                    DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), targetAddress, Constants.SOCKET_UDP_PORT);
                    datagramSocket.send(packet);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void close() {
        if (null == datagramSocket) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }
}
