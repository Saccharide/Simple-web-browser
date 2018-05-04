import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class AppSelectActivity extends AppCompatActivity {

    private ApplicationAdapter applistAdaptor = null;
    NotificationCompat.Builder notification;
    private static final int uniqueID = 7777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_select);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        final PackageManager pm = getPackageManager();
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<ApplicationInfo> packages_filtered = new ArrayList<ApplicationInfo>();

        for (ApplicationInfo packageInfo: packages){
            if(!isSystemPackage(packageInfo)){
                packages_filtered.add(packageInfo);
            }
//            System.out.println(packageInfo.packageName);
        }


        final ListView appList = (ListView)findViewById(R.id.list_view);
        applistAdaptor = new ApplicationAdapter(this, R.layout.snippet_list_row, packages_filtered);
        appList.setAdapter(applistAdaptor);


        final Button button_next = (Button)findViewById(R.id.selectNext);
        button_next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ArrayList<ApplicationInfo> user_selected_app_list = null;
                user_selected_app_list = applistAdaptor.getUserSelectedAppList();

                switch_to_main(user_selected_app_list);
            }
        });

    }

    private boolean isSystemPackage(ApplicationInfo appInfo){
        return(appInfo.flags & ApplicationInfo.FLAG_SYSTEM)!= 0;
    }

    public void switch_to_help(){
        Intent intent = new Intent (  this, HelpActivity.class);
        startActivity(intent);
    }

    public void switch_to_appselect() {
        Intent intent = new Intent(this, AppSelectActivity.class);
        startActivity(intent);
    }

    public void switch_to_main(ArrayList<ApplicationInfo> selected_apps){
//        List<ApplicationInfo> selected_apps = user_selected_app_list;
        Intent intent = new Intent (  this, MainActivity.class);
//        intent.putExtra("SelectedAppList", (ArrayList<ApplicationInfo>)selected_apps);
        SelectedAppList.setAppList(selected_apps);

//        Intent server = new Intent(this, RunningServer.class);
//        startService(server);
        ProxyServer2 proxyServer = new ProxyServer2();
        proxyServer.execute();
        startActivity(intent);
    }

    class ProxyServer2 extends AsyncTask<Void, Void, Void> {

        ServerSocket ServerSocket;

        @Override
        protected Void doInBackground(Void... voids) {

            int port = 8080;	//default
            try {
                ServerSocket = new ServerSocket(port);
                System.out.println("Started on: " + port);
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + port);
                System.exit(-1);
            }

            try {
                Socket socket;
                while ((socket = ServerSocket.accept()) != null) {
                    new ProxyThread2(socket).start();

                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            try {
                ServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

    }

    class ProxyThread2 extends Thread {

        private Socket clientSocket;
        private static final int BUFFER_SIZE = 32768;
        private boolean previousWasR = false;

        public ProxyThread2(Socket client_socket) {
            super("ProxyThread");
            this.clientSocket = client_socket;
        }

        /*****************************************************/
        /* A simple proxy server needs to do the following:  */
        /*     1) Get     request  from client               */
        /*     2) Forward request  to   server               */
        /*     3) Get     response from server               */
        /*     4) Forward response to   client               */
        /*****************************************************/

        @Override
        public void run() {

            HttpURLConnection connection = null;
            BufferedReader rd = null;

            try {

                // Setting the input stream and output stream for future analysis.
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Storing client request
                String inputLine;

                // Setting a flag to see if we are on the first line
                boolean first_line_reached = false;

                // Initialize the url we will get from client socket
                String server_url = "";
                String request    = "";
                String HTTP_ver   = "";
                /*******************************/
                /* Step 1                      */
                /* Getting request from client */
                /*******************************/
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
                        if (tokens.length > 1 ) {
                            request    = tokens[0];
                            server_url = tokens[1];
                            HTTP_ver   = tokens[2];
                        }
                        else {
                            clientSocket.close();
                            return;
                        }
                        System.out.println("-----------------------------------------------");
                        for(int i = 0; i < tokens.length; i++)
                            System.out.println("tokens["+i+"] = "+tokens[i]);

                        break;
                    }
                }


                if (request.equals("CONNECT")) {

                    server_url = processHTTPS(server_url);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream(),
                            "ISO-8859-1");

                    final Socket forwardSocket;
                    try {
                        forwardSocket = new Socket(server_url, 443);
                        System.out.println(forwardSocket);
                    } catch (IOException | NumberFormatException e) {
                        e.printStackTrace();  // TODO: implement catch

                        outputStreamWriter.write(HTTP_ver + " 502 Bad Gateway\r\n");
                        outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                        outputStreamWriter.write("\r\n");
                        outputStreamWriter.flush();
                        return;
                    }
                    try {

                        outputStreamWriter.write(HTTP_ver + " 200 Connection established\r\n");
                        outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                        outputStreamWriter.write("\r\n");
                        outputStreamWriter.flush();

                        Thread remoteToClient = new Thread() {
                            @Override
                            public void run() {
                                forwardData(forwardSocket, clientSocket);
                            }
                        };
                        remoteToClient.start();
                        try {
                            if (previousWasR) {
                                int read = clientSocket.getInputStream().read();
                                if (read != -1) {
                                    if (read != '\n') {
                                        forwardSocket.getOutputStream().write(read);
                                    }
                                    forwardData(clientSocket, forwardSocket);
                                } else {
                                    if (!forwardSocket.isOutputShutdown()) {
                                        forwardSocket.shutdownOutput();
                                    }
                                    if (!clientSocket.isInputShutdown()) {
                                        clientSocket.shutdownInput();
                                    }
                                }
                            } else {
                                forwardData(clientSocket, forwardSocket);
                            }
                        } finally {
                            try {
                                remoteToClient.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();  // TODO: implement catch
                            }
                        }
                    } finally {
                        forwardSocket.close();
                    }
                }
                else if (request.equals("GET")) {

                    sendNotification();
                    try {
                        System.out.println("Making connection to : " + server_url);

                        /***************************/
                        /* Step 2                  */
                        /* Send request to server  */
                        /***************************/

                        URL url = new URL(server_url);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        /* Step 2 complete                    */
                        /* Finish sending request to server   */

                        /*****************************/
                        /* Step 3                    */
                        /* Get response from server  */
                        /*****************************/

                        InputStream is = null;

                        if (connection.getContentLength() > 0) {
                            try {
                                is = connection.getInputStream();
                                rd = new BufferedReader(new InputStreamReader(is));
                            } catch (IOException ioe) {
                                clientSocket.close();
                            }
                        }
                        else{
                            clientSocket.close();
                        }
                        /* Step 3 complete                     */
                        /* Finish getting response from server */


                        /***************************/
                        /* Step 4                  */
                        /* Send response to client */
                        /***************************/

                        byte by[] = new byte[ BUFFER_SIZE ];
                        if (is != null) {
                            int index = is.read(by, 0, BUFFER_SIZE);
                            while (index != -1) {
                                out.write(by, 0, index);
                                index = is.read(by, 0, BUFFER_SIZE);
                            }
                            out.flush();
//                            sendNotification();
                        }

                        /* Step 4 complete                     */
                        /* Finish sending response to client   */

                    } catch (Exception e) {
                        //can redirect this to error log
                        System.err.println("Encountered exception: " + e);
                        //encountered error - just send nothing back, so
                        //processing can continue
                        out.writeBytes("");
                    }
                    finally {
                        //close all resources
                        if (rd != null) {
                            rd.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                        if (clientSocket != null) {
                            clientSocket.close();
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();  // TODO: implement catch
                }
            }

        }

        private String processHTTPS(String url){

            if (url != null) {
                if (url.contains(":443")) {
                    return url.split(":443")[0];
                }

                if (url.contains("https")) {
                    return url.split("https://")[1];
                }
            }
            return url;
        }

        private void forwardData(Socket inputSocket, Socket outputSocket) {
            try {
                InputStream inputStream = inputSocket.getInputStream();
                try {
                    OutputStream outputStream = outputSocket.getOutputStream();
                    try {
                        byte[] buffer = new byte[4096];
                        int read;
                        do {
                            read = inputStream.read(buffer);
                            if (read > 0) {
                                outputStream.write(buffer, 0, read);
                                if (inputStream.available() < 1) {
                                    outputStream.flush();
                                }
                            }
                        } while (read >= 0);
                    } finally {
                        if (!outputSocket.isOutputShutdown()) {
                            outputSocket.shutdownOutput();
                        }
                    }
                } finally {
                    if (!inputSocket.isInputShutdown()) {
                        inputSocket.shutdownInput();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            }
        }

    }
    private void sendNotification() {

        notification = new NotificationCompat.Builder(this);
//        notification.setAutoCancel(true);
        notification.setSmallIcon(R.drawable.ic_warning_black_24dp);
        notification.setTicker("Ticker");
        notification.setWhen(System.currentTimeMillis());

        notification.setContentTitle("Insecure Internet Connection!!");
        notification.setContentText("Using HTTP Protocol :(");

        // Getting the user selected app list and forward it to Main Activity
        ArrayList<ApplicationInfo> user_selected_app_list = null;
        user_selected_app_list = applistAdaptor.getUserSelectedAppList();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SelectedAppList",user_selected_app_list);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(pendingIntent);
        // Builds notification and issues it
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(uniqueID, notification.build());

    }

}
