# live-view-poc

Briefly, this is a proof of concept and tentative implementation of `Live View` feature.

## Directory structure
- Android  
  This directory contains implementation of client side POC in Java ( Android Studio Project ).
- Raspberry Pi  
  This directory contains implementation of server side POC in python.

## Components

### Server Side

##### RaspiMjpeg 
RaspiMjpeg is used to wrap and provide high level interface for Raspberry Pi camera module.  
Within the scope of this POC, we only use its preview feature

##### liveviewfeeder.py
Simple socket that will listen for client (Android) and respond with raw binary image data.

### Client Side

Android application requests for image from the server and shows the result in image view. 
Reusable Bitmap object is implemented using BitmapFactory.Options.inBitmap to solve GC blocking problems. 

## Notes

// Todo





