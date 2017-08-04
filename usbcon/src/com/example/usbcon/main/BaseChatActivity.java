package com.example.usbcon.main;

import com.example.usbcon.R;
import com.example.usbcon.UsbUtils.LogUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public abstract class BaseChatActivity extends Activity {

    private TextView contentTextView;
    private EditText input;
    private Button sendBt;
    public abstract void sendString(final String string);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        contentTextView= (TextView) findViewById(R.id.content_text);
        input= (EditText) findViewById(R.id.input_edittext);
        sendBt = (Button) findViewById(R.id.send_button);
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputString = input.getText().toString();
                if (inputString.length() == 0) {
                    return;
                }
                sendString(inputString);
                printLineToUI(getString(R.string.local_prompt) + inputString);
                input.setText("");
            }
        });
    }
    public void setBtText(String text) {
    	if(sendBt != null)
    		sendBt.setText(text);
    }
    public void printLineToUI(final String line) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	LogUtils.d("printLineToUI"+line);
                contentTextView.setText(contentTextView.getText() + "\n" + line);
            }
        });
    }

}
