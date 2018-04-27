package saccharide.com.simple_web_browser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class HTTP_Thread extends Thread{

    private Socket socket;
    private static final int BUFFER_SIZE = 32768;
    public String server_url;

    public HTTP_Thread(Socket client_socket, String url) {
        super("ProxyThread");
        this.socket = client_socket;
        this.server_url = url;
    }

    /*****************************************************/
    /* A simple proxy server needs to do the following:  */
    /*     1) Get     request  from client               */
    /*     2) Forward request  to   server               */
    /*     3) Get     response from server               */
    /*     4) Forward response to   client               */
    /*****************************************************/
    public void run() {

        try {
            // Setting the input stream and output stream for future analysis.
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /* Step 1 complete                    */
            /* Finish getting request from client */

            BufferedReader rd = null;
            try {
                System.out.println("Making connection to : " + server_url);

                /***************************/
                /* Step 2                  */
                /* Send request to server  */
                /***************************/

                URL url = new URL(server_url);
                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                /* Step 2 complete                    */
                /* Finish sending request to server   */

                /*****************************/
                /* Step 3                    */
                /* Get response from server  */
                /*****************************/

                InputStream is = null;

                if (conn.getContentLength() > 0) {
                    try {
                        is = conn.getInputStream();
                        rd = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException ioe) {
                        socket.close();
                    }
                }
                else{
                    socket.close();
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
            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isHTTP (String url){

        if (url.substring(7).equals("http://"))
            return true;
        else{
            if (url.substring(8).equals("https://"))
                return false;
            else{
                if (url.split(":")[1].equals("443"))
                    return false;
                else
                    return true;
            }
        }

    }

}