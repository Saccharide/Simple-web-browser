package saccharide.com.simple_web_browser;

import android.app.admin.SystemUpdateInfo;
import android.renderscript.ScriptGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;

public class ProxyThread implements Runnable{

    public Socket clientSocket;
    public int id = 0;
    @Override
    public void run() {

        try {

            // Client input output stream
            InputStream  ClientInputStream  = clientSocket.getInputStream();
            OutputStream ClientOutpurStream = clientSocket.getOutputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(ClientInputStream));
            // Setting the rules for timeout
            long timeout   = 10000;
            long heartbeat = System.currentTimeMillis();

            System.out.println("Client #"+id+" has connected at " + System.currentTimeMillis());

            // Getting Client request
            /***********************************************************************************/

            // Storing client request
            String inputLine;

            // Setting a flag to see if we are on the first line
            boolean first_line_reached = false;

            // Initialize the url we will get from client socket
            String server_url = "";
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                // Analyze the first line of the network request from socket to find the url and protocol
                if (!first_line_reached) {
                    String[] tokens = inputLine.split(" ");
                    if (tokens.length > 1 )
                        server_url = tokens[1];
                    else {
                        clientSocket.close();
                        return;
                    }
                    System.out.println("-----------------------------------------------");
                    for(int i = 0; i < tokens.length; i++)
                        System.out.println(tokens[i]);
                    System.out.println("-----------------------------------------------");

                    break;
                }
            }
            System.out.println("-----------------------------------------------");
            System.out.println("Sever URL = " + server_url);
            System.out.println("-----------------------------------------------");
            InetAddress address = InetAddress.getByName(new URL("https://www.google.com").getHost());
            String ip = address.getHostAddress();
            /***********************************************************************************/

            // Creating a socket rather than a HTTP connection
            Socket to_server = new Socket(ip,443);
            InputStream to_serverInputStream = to_server.getInputStream();
            OutputStream to_serverOutputStream = to_server.getOutputStream();


            /***********************************************************************************/
            byte[] received = null;

            while((System.currentTimeMillis() - heartbeat ) < timeout){

                // Put current thread to sleep a bit
                // Reading Client request and sending that request to server
                Thread.sleep(10);

                if (ClientInputStream.available() > 0) {
                    received = new byte[ClientInputStream.available()];
                    ClientInputStream.read(received);

                    // Update the heartbeat
                    heartbeat = System.currentTimeMillis();

                    to_serverOutputStream.write(received, 0, received.length);
                }

                // Read message from server and send to client

                if (to_serverInputStream.available() > 0) {
                    received = new byte[to_serverInputStream.available()];
                    to_serverInputStream.read(received);
                    // Update the heartbeat
                    heartbeat = System.currentTimeMillis();

                    ClientOutpurStream.write(received, 0, received.length);
                }
            }

            System.out.println("Client #"+id+ "has disconnected");

            try{to_server.close();} catch (Exception e) {}
            try{clientSocket.close();} catch (Exception e) {}
            clientSocket = null;
            to_server    = null;


        } catch (IOException e) {
            System.out.println("Input / Output has been interrupted!");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Thread has been interrupted!");
            e.printStackTrace();
        }


    }
}
