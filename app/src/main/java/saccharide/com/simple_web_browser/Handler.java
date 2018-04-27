package saccharide.com.simple_web_browser;

import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;


public class Handler extends Thread {

    private final Socket clientSocket;
    private boolean previousWasR = false;

    public Handler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

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

                    byte by[] = new byte[ 32768 ];
                    if (is != null) {
                        int index = is.read(by, 0, 32768);
                        while (index != -1) {
                            out.write(by, 0, index);
                            index = is.read(by, 0, 32768);
                        }
                        out.flush();
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

    private static void forwardData(Socket inputSocket, Socket outputSocket) {
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
