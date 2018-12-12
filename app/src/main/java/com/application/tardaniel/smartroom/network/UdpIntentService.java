package com.application.tardaniel.smartroom.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.application.tardaniel.smartroom.R;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class UdpIntentService extends IntentService {

    // IntentService can perform the following actions:
    private static final String ACTION_SEND_COLOR =
            "com.application.tardaniel.smartroom.network.action.SEND_COLOR";
    private static final String ACTION_SEND_PACKET =
            "com.application.tardaniel.smartroom.network.action.SEND_PACKET";
    private static final String ACTION_SET_LOCAL_IP_ADDRESS =
            "com.application.tardaniel.smartroom.network.action.SET_LOCAL_IP";

    // Local variables
    private static final String COLOR = "COLOR";
    private static final String MODE = "MODE";
    private static final String LOCAL_IP_ADDRESS = "LOCAL_IP_ADDRESS";

    private static InetAddress sLocalAddress;
    //    private static final int sPort = 55056;
    private static final int sPort = 1302;

    private static long mTime1 = 0;
    private static long mTime2 = 0;
    private static final long DELTA = 45;


    //for making toast messages
    //Handler mHandler;


    public UdpIntentService() {
        super("UdpIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mHandler = new Handler();
    }

    /**
     * Starts this service to perform an UDP message transaction to the LED strip with the given parameters.
     * If the service is already performing a task this action will be queued.
     */
//    public static void sendPacket(Context context, String color, String mode) {
//        Intent intent = new Intent(context, UdpIntentService.class);
//        intent.setAction(ACTION_SEND_PACKET);
//        intent.putExtra(COLOR, color);
//        intent.putExtra(MODE, mode);
//        context.startService(intent);
//    }

    /**
     * Starts this service to perform an UDP message transaction to the LED strip with the given parameters.
     * If the service is already performing a task this action will be queued.
     */
    public static void sendPacket(Context context, String color, String mode) {
        mTime2 = System.currentTimeMillis();
        if (mTime2 - mTime1 > DELTA) {
            Intent intent = new Intent(context, UdpIntentService.class);
            intent.setAction(ACTION_SEND_PACKET);
            intent.putExtra(COLOR, color);
            intent.putExtra(MODE, mode);
            mTime1 = System.currentTimeMillis();
            context.startService(intent);
        }
    }

    public static void sendColor(Context context, int color, int mode) {
        mTime2 = System.currentTimeMillis();
        if (mTime2 - mTime1 > DELTA) {
            Intent intent = new Intent(context, UdpIntentService.class);
            intent.setAction(ACTION_SEND_COLOR);
            intent.putExtra(COLOR, color);
            intent.putExtra(MODE, mode);
            mTime1 = System.currentTimeMillis();
            context.startService(intent);
        }
    }

    public static void setLocalIpAddress(Context context, String ip) {
        Intent intent = new Intent(context, UdpIntentService.class);
        intent.setAction(ACTION_SET_LOCAL_IP_ADDRESS);
        intent.putExtra(LOCAL_IP_ADDRESS, ip);
        context.startService(intent);
    }

    /**
     * Handels the incoming requests and call the appropriate function (handleActionSendCommand or handleActionSetLocalIpAddress)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_COLOR.equals(action)) {
                final int color = intent.getIntExtra(COLOR, 0xffffffff);
                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = (color & 0x000000ff);
                final int mode = intent.getIntExtra(MODE, 0);
                handleSendRgb(red, green, blue, mode);
            } else if (ACTION_SEND_PACKET.equals(action)) {

                final String color = intent.getStringExtra(COLOR);
                final String mode = intent.getStringExtra(MODE);
                handleActionSendCommand(color, mode);
                //handleActionSendCommand(intent.getStringExtra(COLOR), intent.getStringExtra(MODE));

            } else if (ACTION_SET_LOCAL_IP_ADDRESS.equals(action)) {
                final String param1 = intent.getStringExtra(LOCAL_IP_ADDRESS);
                handleActionSetLocalIpAddress(param1);
            }
        }
    }

    private void handleSendRgb(final int red, final int green, final int blue, final int mode) {
        try {
            //int packet = packet0 << 24 | packet1 << 16 | packet2 << 8 | packet3;
            //String msg = color + " " + mode;

            //int[] packet = new int[3];
            byte[] message = new byte[5];
            message[0]=(byte)233; //security byte
            message[1]=(byte)mode;
//            message[1]=0;
            message[2]=(byte)red;
            message[3]=(byte)green;
            message[4]=(byte)blue;

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket p = new DatagramPacket(message,message.length,sLocalAddress,sPort);
            socket.send(p);

            /**int[] packet = new int[4];
            byte[][] message = new byte[4][1];
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket p = new DatagramPacket(message[0],1);
            p.setAddress(sLocalAddress);
            p.setPort(sPort);
            for (int i = 0; i < 4; ++i) {
                packet[0] = (0b100000000 | red) >> 1;
                packet[1] = 0b10000000 | ((red & 0b00000001) << 6) | ((green & 0b11111100) >> 2);
                packet[2] = 0b10000000 | ((green & 0b00000011) << 5) | ((blue & 0b11111000) >> 3);
                packet[3] = 0b01111111 & (((blue & 0b00000111) << 4) | (mode & 0b00001111));

                message[0][0] = (byte) packet[0];
                message[1][0] = (byte) packet[1];
                message[2][0] = (byte) packet[2];
                message[3][0] = (byte) packet[3];


                //socket.setBroadcast(true);
                p.setData(message[3-i],0,1);
                socket.send(p);
                SystemClock.sleep(10);
            }

            Log.d("rgb+mode", red + " " + green + " " + blue + " " + mode);
            Log.d(UdpIntentService.class.getSimpleName(), packet[0] + " " + packet[1] + " " + packet[2] + " " + packet[3]);
            */
        } catch (Exception e) {
            Log.e(getString(R.string.tag_is), getString(R.string.tag_is_description) + " " + e.toString());
        }
    }

    /**
     * Handle action SendCommand in the provided background thread with the color and mode
     * parameters.
     */
    private void handleActionSendCommand(final String color, final String mode) {
        // TODO: convert message to the micro controller protocol
        try {
            String msg = color + " " + mode;
            byte[] message = msg.getBytes();

            // In case of broadcast
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            DatagramPacket p = new DatagramPacket(message, msg.length(), sLocalAddress, 55056);
            socket.send(p);

            /**multicast_test*/
//            InetAddress group = InetAddress.getByName("192.168.0.255");
//            MulticastSocket s = new MulticastSocket(10110);
//            s.joinGroup(group);
//
//            DatagramPacket mp = new DatagramPacket(message,
//                    msg.length(), group, 10110);
//
//            s.leaveGroup(group);

            /**toast making*/
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                Toast.makeText(UdpIntentService.this, color + " " + mode, Toast.LENGTH_SHORT).show();
//            }
//        });
            Log.d(UdpIntentService.class.getSimpleName(), color + " " + mode);
        } catch (Exception e) {
            Log.e(getString(R.string.tag_is), getString(R.string.tag_is_description) + " " + e.toString());
        }

    }

    private void handleActionSetLocalIpAddress(String ip) {
        try {
            sLocalAddress = InetAddress.getByName(ip);
            Log.d(UdpIntentService.class.getSimpleName(), getString(R.string.tag_is_description_ip_set) + " " + ip);
        } catch (UnknownHostException e) {
            Log.e(UdpIntentService.class.getSimpleName(), getString(R.string.set_ip_error));
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    //Toast.makeText(UdpIntentService.this, R.string.set_ip_error, Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }
}
