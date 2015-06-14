package me.hibikiledo.liveviewpoc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class LiveViewUpdater extends Thread {

    private Context context;
    private boolean failed = false;

    // reusable bitmap object to solve GC blocking problem
    private static Bitmap inBitmap = null;
    // URL where jpeg image can be retrieved
    private final static String imageURL = "http://192.168.1.15/cam_pic.php";


    public LiveViewUpdater(Context context) {
        this.context = context;
    }

    @Override
    public void run() {

        // Set priority to background so that it won't compete with UI thread
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        // Create option object for decoder
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // inBitmap works with mutable only
        options.inMutable = true;

        Log.d(LiveViewActivity.TAG, "LiveViewUpdater started.");

        while( !failed ) {

            // if there is bitmap object, reuse it via options.inBitmap
            if( inBitmap != null ) {
                options.inBitmap = inBitmap;
            }

            // Read bitmap from URL

            InputStream is = null;

            try {

                // connect to image source
                URL url = new URL(imageURL);
                is = url.openConnection().getInputStream();

                // use our options with decodeStream to get 'mutable' Bitmap object
                inBitmap = BitmapFactory.decodeStream(is, null, options);

            } catch( MalformedURLException e ) {

                Log.d(LiveViewActivity.TAG, e.getMessage());
                failed = true;
                break;

            } catch( IOException e ) {

                Log.d(LiveViewActivity.TAG, e.getMessage());
                failed = true;
                break;

            } finally {

                try {
                    if (is != null) is.close();
                } catch( IOException e ) {
                    Log.d(LiveViewActivity.TAG, e.getMessage());
                    failed = true;
                    break;
                }

            }

            // Send message to UI thread
            if( inBitmap != null ) {

                // Create bundle and message
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putInt(LiveViewActivity.MSG, LiveViewActivity.LIVEVIEW_MSG);
                msg.setData(bundle);

                // send message to main thread's handler
                ((LiveViewActivity) context).setLiveViewData(inBitmap);
                ((LiveViewActivity) context).getHandler().sendMessage(msg);

            }
        }
    }

}