package saccharide.com.simple_web_browser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

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

    public void send_web_request(String url, TextView user_view){

        user_view.setText(url);

    }
}
