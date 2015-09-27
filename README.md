# live-view-poc

Briefly, this is a proof of concept and tentative implementation of `Live View` feature.

## Directory structure
- Android  
  This directory contains implementation of client side POC in Java ( Android Studio Project ).

## Components

### Client Side

Android application requests for image from the server and shows the result in image view. 
Reusable Bitmap object is implemented using BitmapFactory.Options.inBitmap to solve GC blocking problems. 

## Notes

Server side has been moved to another repository which can be found [here](https://github.com/hibikiledo/A-Liveview-Feeder).





