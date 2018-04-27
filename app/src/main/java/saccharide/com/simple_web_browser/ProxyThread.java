package saccharide.com.simple_web_browser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ProxyThread implements Runnable{

    public Socket clientSocket;
    public int id = 0;
    @Override
    public void run() {

        try {
            InputStream ClientInputStream = clientSocket.getInputStream();
            // Setting the rules for timeout
            long timeout   = 10000;
            long heartbeat = System.currentTimeMillis();



        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
