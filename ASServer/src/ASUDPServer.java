import sun.rmi.runtime.Log;

import java.io.IOException;
import java.net.*;
import java.util.Date;

import static java.awt.SystemColor.info;

/**
 * Created by Sergio on 23/09/2016.
 */
public class ASUDPServer extends Thread {

    private ASServer.OnMessageReceived messageListener;

    public static final int SERVERPORT_UDP = 4222;
    private DatagramSocket ds;
    boolean running;
    private ASServer asserver;
    private String clientIp;

    public ASUDPServer(ASServer.OnMessageReceived listener, ASServer server) throws SocketException {
        messageListener = listener;
        asserver = server;
    }

    public void sendMessageUDP(String message)
    {
        InetAddress serverAddr = null;

        try {
            serverAddr = InetAddress.getByName(clientIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), serverAddr, 4000);
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void run()
    {
        running = true;
        try {
            byte[] lMsg = new byte[512];
            DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
            ds = new DatagramSocket(SERVERPORT_UDP);
            boolean first = true;
            while (running) {

                ds.receive(dp);

                String message = new String(lMsg, 0, dp.getLength());
                //  String answer = "RECEIVED -- " + message;
                if (first)
                {
                    clientIp = message;
                    first = false;
                }
                sendMessageUDP("RECEIVED -- " + message);

                if (message != null && messageListener != null)
                {
                    //call the method messageReceived from ServerBoard class
                    messageListener.messageReceived(message);
                    System.out.println(new Date() + " - Received through UDP - " + message);
                }

            }
//            Log.i("UDP packet received", lText);
//            textView.setText(lText);

        }

        catch (SocketException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (ds != null) {
                ds.close();
            }
        }
    } }
