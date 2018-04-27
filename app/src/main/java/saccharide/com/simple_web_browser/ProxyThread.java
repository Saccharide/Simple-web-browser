package saccharide.com.simple_web_browser;

import android.app.admin.SystemUpdateInfo;
import android.renderscript.ScriptGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ProxyThread implements Runnable{

    public Socket clientSocket;
    public int id = 0;
    @Override
    public void run() {

        try {

            // Client input output stream
            InputStream  ClientInputStream  = clientSocket.getInputStream();
            OutputStream ClientOutpurStream = clientSocket.getOutputStream();

            // Setting the rules for timeout
            long timeout   = 10000;
            long heartbeat = System.currentTimeMillis();

            System.out.println("Client #"+id+" has connected at " + System.currentTimeMillis());

            // Getting Client request
            /***********************************************************************************/






            /***********************************************************************************/

            // Creating a socket rather than a HTTP connection
            Socket to_server = new Socket("127.0.0.1",80);
            InputStream to_serverInputStream = to_server.getInputStream();
            OutputStream to_serverOutputStream = to_server.getOutputStream();


            /***********************************************************************************/
            byte[] received = null;
            byte[] message  = new byte[]{};

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
