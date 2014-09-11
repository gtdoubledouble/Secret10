package com.example.secret10;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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

public class SecretListActivity extends Activity  {
	
	private WebSocketClient mWebSocketClient;

    ListView listView;
    List<String> values = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sampleclient);

        listView = (ListView) findViewById(R.id.list);
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
		
		connectWebSocket();


        // populate list view with array of strings
        // define an adapter and attach it to listview
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
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
	
	private void connectWebSocket() {
		Log.i("Websocket", "initiate");
		  URI uri;
		  try {
		    uri = new URI("ws://192.168.1.68:8080");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
		    return;
		  }

		  mWebSocketClient = new WebSocketClient(uri) {
		    @Override
		    public void onOpen(ServerHandshake serverHandshake) {
		      Log.i("Websocket", "Opened");
		      // mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
		    }

		    @Override
		    public void onMessage(String s) {
		      final String message = s;
              String eventType = null;
              try {
                  final JSONObject obj = new JSONObject(s);
                  eventType = obj.getString("event");
                  // if new message
                  if(!eventType.isEmpty()) {
                      Log.i("Websocket", eventType);
                      if(eventType.equals("message")) {
                          runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  try {
                                      String msg = obj.getString("data");
                                      String sender = obj.getString("user");
                                      addToList(sender + ": " + msg);
                                  } catch(Exception e){};

                              }
                          });
                      } else if(eventType.equals("update_userlist")) {
                          Log.i("Websocket", "update_userlist");
                          final Context context = getApplicationContext();
                          final CharSequence text = "user joined or left";
                          final int duration = Toast.LENGTH_SHORT;

                          runOnUiThread(new Runnable() {
                              public void run() {
                                  Toast toast = Toast.makeText(context, text, duration);
                                  toast.show();
                              }
                          });
                      }
                  }
              } catch( JSONException e ) {
                  Log.i("Websocket", "Invalid JSON message");
              };

		      Log.i("Websocket", s);



		    }

		    @Override
		    public void onClose(int i, String s, boolean b) {
		      Log.i("Websocket", "Closed " + s);
		    }

		    @Override
		    public void onError(Exception e) {
		      Log.i("Websocket", "Error " + e.getMessage());
		    }
		  };
		  
		  Log.i("Websocket", "connecting");
		  mWebSocketClient.connect();
		}
	
	public void sendMessage() {
		EditText editText = (EditText)findViewById(R.id.message);
        String msg = editText.getText().toString();
		mWebSocketClient.send(msg);
		editText.setText("");

        addToList("Me: " + msg);
    }

    public void addToList(String msg) {
        values.add(msg);
        Log.i("Websocket", "sent message = " + msg);
        listView.invalidateViews();
    }

}
