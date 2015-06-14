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
import java.net.Socket;
import java.net.URL;

public class LiveViewUpdaterSocket extends Thread {

    private Context context;
    private boolean failed = false;

    // reusable bitmap object to solve GC blocking problem
    private static Bitmap inBitmap = null;

    // IP and PORT of the server
    final private static String IP = "192.168.1.15";
    final private static int PORT = 5000;
    // refresh rate in ms
    final private static int refreshRate = 0;

    public LiveViewUpdaterSocket(Context context) {
        this.context = context;
    }

    @Override
    public void run() {

        // Set priority
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        // Create option object for decoder
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // inBitmap works with mutable only
        options.inMutable = true;

        Log.d(LiveViewActivity.TAG, "LiveViewUpdaterSocket started.");

        while( !failed ) {

            // if there is bitmap object, reuse it via options.inBitmap
            if( inBitmap != null ) {
                options.inBitmap = inBitmap;
            }

            // Read bitmap from URL

            InputStream is = null;
            Socket socket = null;

            try {
                socket = new Socket(IP, PORT);
                is = socket.getInputStream();

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
                    if (socket != null) {
                        socket.shutdownInput();
                        socket.close();
                    }
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

            // For fine adjustment between performance and smoothness of LiveView
            try {
                Thread.sleep(refreshRate, 0);
            } catch (InterruptedException e) {
                Log.d(LiveViewActivity.TAG, e.getMessage());
            }

        }

        Log.d(LiveViewActivity.TAG, "LiveViewUpdaterSocket stopped.");

    }
}