package me.hibikiledo.liveviewpoc;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class LiveViewActivity extends ActionBarActivity {

    final public static String TAG = "me.hibikiledo.DEBUG";
    final public static String MSG = "me.hibikiledo.MESSAGE";

    final public static int LIVEVIEW_MSG = 0;

    private ImageView imageView;
    private Bitmap imageData;

    private LiveViewUpdaterSocket updater;

    /*
        Handler for UI thread
            This allows LiveViewUpdater to set new imageData and trigger update
     */
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_view);

        // create handler
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int msgType = msg.getData().getInt(MSG);
                if( msgType == LIVEVIEW_MSG ) {
                    imageView.setImageBitmap( imageData );
                }
                return false;
            }
        });

        // Locate image view
        imageView = (ImageView) findViewById(R.id.liveview);

        // Creating new thread for refreshing ImageView
        updater = new LiveViewUpdaterSocket(this);
        updater.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_view, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called. Stopping updater thread ..");
        updater.kill();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called. Starting updater thread if none exist ..");
        if( ! updater.isAlive() ) {
            updater = new LiveViewUpdaterSocket(this);
            updater.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    // Implementation for handling live view updates
    protected void setLiveViewData(Bitmap imageData) {
        this.imageData = imageData;
    }

    public Handler getHandler() {
        return this.handler;
    }

}