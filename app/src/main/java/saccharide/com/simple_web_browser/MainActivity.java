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
import java.net.Proxy;
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


        user_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_url.setText("");
            }
        });

        // Real proxy stuff here

        ProxyServer proxyServer = new ProxyServer();



    }

}
