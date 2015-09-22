package me.hibikiledo.liveviewpoc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;

public class LiveViewUpdaterSocket extends Thread {

    private Context context;
    private boolean failed = false;
    private boolean killed = false;

    // IP and PORT of the server
    final private static String IP = "10.0.2.1";
    final private static int PORT = 1234;

    // Thread sleep time between each request
    final private static int refreshRate = 5;

    // Create option object for decoder
    final BitmapFactory.Options options = new BitmapFactory.Options();

    // inBitmap and buffer is used for caching and performance improvement
    private static Bitmap inBitmap = null;
    private static byte[] buffer = new byte[16 * 1024];

    public LiveViewUpdaterSocket(Context context) {
        this.setName("Bitmap Updater");
        this.context = context;

        // inBitmap works with mutable only
        options.inMutable = true;
        // assign buffer to avoid reallocating 16K buffer
        options.inTempStorage = buffer;
    }

    @Override
    public void run() {

        // Set priority
        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);


        Log.d(LiveViewActivity.TAG, "LiveViewUpdaterSocket started.");

        while( !failed && !killed ) {

            /*
                Reassign inBitmap field with the previous decoded Bitmap.
                According to the doc, decodeStream will reuse our Bitmap object.

                // Note // Reading the source code confirmed this already.
             */
            if( inBitmap != null ) {
                options.inBitmap = inBitmap;
            }

            /*
                Scope stuffs .. it has to be here That's it !
             */
            InputStream is = null;
            Socket socket = null;

            /*
                Get new Bitmap data from input stream.
                This requires us to allocate Socket for every request.

                We use decodeStream with Bitmap.Options so that we can
                reuse buffer and also Bitmap.
             */
            try {

                socket = new Socket(IP, PORT);
                is = socket.getInputStream();

                // Decode bitmap from stream
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

            /*
                Send new Bitmap from stream via handler on UI Thread
                via a Message and Bundle.
             */
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

            /*
                Let's thread wait and do nothing for value in ms
                specified in refreshRate. This allows us to adjust thread not
                to be too aggressive consuming all the cpu time.
             */
            try {
                // If it's killed, no need to sleep, let it die fast.
                if(!killed) {
                    Thread.sleep(refreshRate, 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        Log.d(LiveViewActivity.TAG, "LiveViewUpdaterSocket stopped.");

    }

    public void kill() {
        this.killed = true;
    }

}