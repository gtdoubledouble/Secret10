package com.example.secret10;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity  {

    ListView listView;
    public static List<String> messageList = new ArrayList<String>();
    String targetUsername;
    String targetUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        listView = (ListView) findViewById(R.id.messageList);
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // set action bar name based on the passed in contact name (from bundle)
        try {
            targetUsername = getIntent().getStringExtra("username");
            targetUserID = getIntent().getStringExtra("userID");
            setTitle(targetUsername);

            // populate list view with chat messages, by retrieving the chat log from sharedPreferences
            SharedPreferences chatDocs = getSharedPreferences("chatDocs", 0);
            String chatLog = chatDocs.getString(targetUserID, "");
            JSONArray chatMessages = new JSONArray(chatLog);

            // convert String -> JSONArray -> insert each message into ArrayList
            for (int i = 0; i < chatMessages.length(); i++) {
                String msg = chatMessages.getString(i);
                messageList.add(msg);
            }

        } catch(Exception e) {
            Log.e("chat", e.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messageList);
        listView.setAdapter(adapter);

        // set listener on listView
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),
                        (String) listView.getItemAtPosition(position), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public void sendMessage() {
        EditText editText = (EditText)findViewById(R.id.messageField);
        String msg = editText.getText().toString();

        // construct appropriate JSON object as a string
        String messageObj = "{\"event\":\"message\", " +
                            "\"data\": { \"from\": \"" + InitialActivity.myUserID + "\" ," +
                                         "\"to\" : \"" + targetUserID + "\"," +
                                         "\"message\": \"" + msg + "\"} }";
        Log.i("Websocket", "sending out this message : " + messageObj);
        SecretListActivity.mWebSocketClient.send(messageObj);
        editText.setText("");
    }

}
