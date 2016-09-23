/**
 * Created by Sergio on 21/09/2016.
 */
import javax.swing.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/**
 * The class extends the Thread class so we can receive and send messages at the same time
 */
public class ASServer extends Thread {

    public static final int SERVERPORT = 4221;
    public static final int SERVERPORT_UDP = 4222;
    private boolean running = false;
    private PrintWriter mOut;
    private OnMessageReceived messageListener;

    //Stores the received info here
    ArrayList<String> info;

    public static void main(String[] args) {

        //opens the window where the messages will be received and sent
        ServerBoard frame = new ServerBoard();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * Constructor of the class
     * @param messageListener listens for the messages
     */
    public ASServer(OnMessageReceived messageListener) {
        info = new ArrayList<String>();
        this.messageListener = messageListener;
    }

    /**
     * Method to send the messages from server to client
     * @param message the message sent by the server
     */
    public void sendMessage(String message){
        if (mOut != null && !mOut.checkError()) {
            mOut.println(message);
            mOut.flush();
        }
    }

    @Override
    public void run() {
        super.run();

        running = true;

        try {
            System.out.println("S: Initiating TCP Server...");

            //create a server socket. A server socket waits for requests to come in over the network.
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
    System.out.println("Initiated.");

            System.out.println("S: Initiating UDP Server...");

            byte[] lMsg = new byte[512];
            DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
            DatagramSocket ds = new DatagramSocket(SERVERPORT_UDP);
            
            System.out.println("Initiated.");

            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
            Socket client = serverSocket.accept();
            System.out.println("S: Receiving...");

            try {

                //sends the message to the client
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                //read the message received from client
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                boolean connectionEstablished = false;
                //in this while we wait to receive messages from client (it's an infinite loop)
                //this while it's like a listener for messages
                while (running) {
                    String message = in.readLine();

                    if(!connectionEstablished)
                    {
                            //First message must be client ip
                            sendMessage("HAIL2U, " + message + ", let's TCP");
                            connectionEstablished=true;

                    }

                    info.add(message);

                    sendMessage("RECEIVED -- " + message);

                    if (message != null && messageListener != null)
                    {
                        //call the method messageReceived from ServerBoard class
                        messageListener.messageReceived(message);
                    }

                    System.out.println(new Date() + " - Received - " + message);
                    //add protocol
                }

            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            } finally {
                client.close();
                System.out.println("S: Done.");
            }

        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the ServerBoard
    //class at on startServer button click
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
