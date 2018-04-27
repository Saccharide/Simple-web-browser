package saccharide.com.simple_web_browser;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer extends AsyncTask<Void, Void, Void> {

    ServerSocket serverSocket;
    int id = 0;
    @Override
    protected Void doInBackground(Void... voids) {

        // Listening to port 8080
        int port = 8080;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Proxy Server has started on port : " + port);

            while (true){
                Socket clientSocket = serverSocket.accept();
                ProxyThread proxyThread = new ProxyThread();
                Thread thread = new Thread(proxyThread);

                proxyThread.clientSocket = clientSocket;
                proxyThread.id = ++id;
                thread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
