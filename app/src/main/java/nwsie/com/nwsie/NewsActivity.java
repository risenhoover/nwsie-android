package nwsie.com.nwsie;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import nwsie.com.com.R;

public class NewsActivity extends AppCompatActivity {

    WebSocketClient mWebSocketClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);


        connectWebSocket();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://www.nwsie.com/topicWS/all");
        } catch (URISyntaxException e) {
            Log.e("Websocket", e.getLocalizedMessage());
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;

                JSONObject o = null;
                try {
                    o = new JSONObject(message);
                } catch (JSONException e) {
                    Log.e("JSON", e.getLocalizedMessage());
                    return;
                }

                final JSONObject post = o;
                Log.d("JSON", post.toString());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        RelativeLayout top = (RelativeLayout) findViewById(R.id.top_layout);
                        top.setBackgroundColor(Color.WHITE);
                        //getApplication().setTheme(R.style.NewsTheme);

                        TableLayout table = (TableLayout) findViewById(R.id.news_layout);

                        Context c = table.getContext();

                        try {

                            LinearLayout row = new LinearLayout(c);
                            row.setOrientation(LinearLayout.VERTICAL);
                            row.setGravity(Gravity.CENTER);

                            ImageView imageView = new ImageView(c);
                            new ImageDownloader(imageView).execute(post.getString("imageUrl"));

                            row.addView(imageView);

                            TextView title = new TextView(c);

                            title.setText(Html.fromHtml("<a href='" + post.get("url") + "'>" + post.getString("title") + "</a>"));

                            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                            title.setGravity(Gravity.LEFT);
                            title.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                            title.setMovementMethod(LinkMovementMethod.getInstance());
                            row.addView(title);

                            TextView desc = new TextView(c);
                            desc.setText(post.getString("excerpt"));
                            desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
                            desc.setGravity(Gravity.LEFT);
                            row.addView(desc);

                            /**
                             * Add the new row at the top, other stuff scrolls down.
                             */
                            table.addView(row, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

                            if (table.getChildCount() > 5) {
                                table.removeViewAt(table.getChildCount()-1);
                            }
                            Log.d("GUI", "Row count: " + table.getChildCount());

                        } catch (JSONException e) {
                            Log.e("JSON", e.getLocalizedMessage());
                        }

//                        while ( table.getChildCount() > 5) {
//                            table.removeViewAt(table.getChildCount()-1);
//                        }

                    }
                });
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
        mWebSocketClient.connect();
    }
}
