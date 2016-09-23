package asteam.asclient;

import java.io.BufferedReader;
import java.io.PrintWriter;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import asteam.asclient.Manager;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ASClient {


    private String serverMessage;
    public static final String SERVERIP = "192.168.0.13"; //your computer IP address
    public static final int SERVERPORT_TCP = 4221;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;
    Manager manager;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public ASClient(OnMessageReceived listener, Manager manager) {
        mMessageListener = listener;
        this.manager = manager;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT_TCP);

            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                boolean connectionEstablished = false;

                sendMessage("HALO");

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();

                    if(!connectionEstablished)
                    {
                        if (serverMessage.equals("HAIL2U"))
                        {
                            connectionEstablished=true;
                            sendMessage("SENDING:LOCATION");
                        }
                    }

                    else
                    {
                        //Format: Longitude:Latitude:Altitude:Velocity
                        //Manager.u
                        Log.i("funciono", "está intentando enviar info");
                        manager.updatePosition();
                        String locationInfo = Manager.getLongitude() + ":" + Manager.getLatitude() + ":" + Manager.getAltitude() + ":" + Manager.getVelocity();
                        sendMessage(locationInfo);
                        Thread.sleep(1000);
                    }

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from Manager class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}