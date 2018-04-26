package saccharide.com.simple_web_browser;

import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    Button web_url;
    EditText user_url;
    TextView server_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web_url = (Button) findViewById(R.id.connect_button);
        user_url = (EditText)findViewById(R.id.user_url);
        server_content = (TextView) findViewById(R.id.content);
        web_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_web_request(user_url.getText().toString(),server_content);
            }
        });

        user_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_url.setText("");
            }
        });
    }

    public void send_web_request(String user_input_url, TextView user_view){

        String content = "Trying to connect to: " + user_input_url + "\n";
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(user_input_url);
            // Trying to oonnect to url;
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Get url's response
            InputStream inputStream = connection.getInputStream();

            // Create a buffer to read the input stream
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine())!= null){
                stringBuffer.append(line);
            }
            content += stringBuffer.toString();

            user_view.setText(content);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                // Close our connection and resources
                if (connection != null)
                    connection.disconnect();
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
