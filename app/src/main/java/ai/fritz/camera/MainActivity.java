package ai.fritz.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.fritzvisionobjectmodel.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static java.lang.Thread.sleep;

public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    //private boolean WIRSisBound = false;
    //WatsonImageRecognitionService WIRS = null;

    private List<RectF> boxes = new ArrayList<RectF>();
    private AtomicBoolean lock = new AtomicBoolean();

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private AtomicBoolean computing = new AtomicBoolean(false);

    // STEP 1:
    // TODO: Define the predictor variable
     private FritzVisionObjectPredictor predictor;
     private FritzVisionImage fritzVisionImage;
     private FritzVisionObjectResult objectResult;
    // END STEP 1

    private Size cameraViewSize;
    private Set<String> labels;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fritz
        Fritz.configure(this);

        FritzOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel();
        predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);

        labels = new HashSet<String>();
        labels.add("chair");
        labels.add("couch");
        labels.add("potted plant");
        labels.add("bed");
        labels.add("dining table");
        labels.add("mirror");
        labels.add("desk");
        labels.add("toilet");
        //labels.add("door");
        labels.add("sink");
        labels.add("vase");
        labels.add("blanket");
        labels.add("carpet");
        labels.add("counter");
        labels.add("cabinet");
        labels.add("pillow");
        labels.add("rug");
        labels.add("table");
        labels.add("desk");

        // Bind to services
        //Intent i = new Intent(this, WatsonImageRecognitionService.class);
        //bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
        /*
        View mCameraView = findViewById(R.id.texture);
        mCameraView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                // Check where we touched
                //1440 x 2560 pixel screen
                // 640 x 480 canvas min....
                float x = event.getX() / 2.25f;
                float y = ((event.getY() / 5.33f)); //+ 480) % 481; //slight miscalc in top right corner
                boolean locked = lock.getAndSet(true);
                if(locked){
                    int boxIndex = -1;
                    // If inside a box, segment the image using first box selected (overlap issue)
                    for(RectF box : boxes){
                        Log.d("TOUCHXY", "(" + Float.toString(x) + ", " + Float.toString(y) + ")");
                        Log.d("TOUCHLEFT", Float.toString(box.left));
                        Log.d("TOUCHRIGHT", Float.toString(box.right));
                        Log.d("TOUCHTOP", Float.toString(box.top));
                        Log.d("TOUCHBOTTOM", Float.toString(box.bottom));
                        Log.d("TOUCH", " ");
                        if(x >= box.left && x <= box.right && y >= box.bottom && y <= box.top){
                            Log.d("INSIDE", "true");
                            boxIndex = boxes.indexOf(box);
                            break;
                        } else {
                            Log.d("INSIDE", "false");
                        }
                    }
                    // Segment image using selected box
                    Bitmap image = null;
                    TextureView currentImg = findViewById(R.id.texture);
                    // bitmap is 1440 by 2272 for width and height
                    // I think the y values are being shrunk due to the ui elements
                    // Note that we have to thus use a different scalar for the y values
                    // Than what we used for touch detection
                    if(currentImg.isAvailable() && boxIndex != -1) {
                        //Log.d("IMAGE", "Image found.");
                        image = currentImg.getBitmap();
                        Log.d("IMAGE1", "(" + Integer.toString(image.getWidth()) + ", " + Integer.toString(image.getHeight()) + ")");
                        RectF box = boxes.get(boxIndex);

                        if(box.bottom < 0){
                            box.bottom = 0;
                        }
                        if(box.top > 480){
                            box.top = 480;
                        }
                        if(box.left < 0){
                            box.left = 0;
                        }
                        if(box.right > 640){
                            box.right = 640;
                        }
                        int startX = Math.round(box.left*2.25f);
                        int startY = Math.round(box.bottom*5.33f -288); //-300 or so?

                        // X may not be scaled correctly or bounding boxes are lies...
                        // Bitmap pixel size is different than width and height...
                        int widthX = Math.round((box.right - box.left)*2.25f + 300); // - startX
                        int heightY = Math.round((box.top - box.bottom)*5.33f -288);// - (2272 - startY));

                        Log.d("BITMAP", Integer.toString(startX));

                        Log.d("BITMAP", Integer.toString(startY));

                        Log.d("BITMAP", Integer.toString(widthX));

                        Log.d("BITMAP", Integer.toString(heightY));
                        Log.d("BITMAP", " ");

                        if(startX > 0 && startY >0 && widthX > 0 && heightY > 0 && ((startY + heightY) < image.getHeight()) && ((startX + widthX) < image.getWidth())){
                            Bitmap resized = Bitmap.createBitmap(image, startX, startY, widthX, heightY);
                            int breakpoint_here = 0;
                            Log.d("IMAGEr", "(" + Integer.toString(resized.getWidth()) + ", " + Integer.toString(resized.getHeight()) + ")");
                            // Classify segmented image
                            //if(WIRSisBound){
                                //Log.d("SERVICE", WIRS.getCurrentTime());
                                // save bitmap as image file
                            String location = getApplicationContext().getFilesDir().toString();

                            final File imageFile = new File(location + "/subImage");
                            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                                resized.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                // PNG is a lossless format, the compression factor (100) is ignored
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d("SAVED", imageFile.getAbsolutePath().toString());

                            // Classify image
                            FurnitureImageClassifier imageClassifier = new FurnitureImageClassifier();
                            imageClassifier.execute(imageFile);




                            //Log.d("CLASSIFY", WIRS.classifyImage(imageFile));
                            //} else {
                            //    Log.d("SERVICE", "Image service not bound");
                            //}
                            // Use Ikea API and update recyclerview
                        }


                    }
                    lock.set(false);
                }
                break;
        }
        return true;
    }

    private Handler mainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            Log.d("MESSAGE", msg.obj.toString());
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_stylize;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size previewSize, final Size cameraViewSize, final int rotation) {

        this.cameraViewSize = cameraViewSize;

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        if (objectResult != null) {
                            List<RectF> newBoxes = new ArrayList<>();
                            for (FritzVisionObject object : objectResult.getVisionObjects()) {
                                if(labels.contains(object.getVisionLabel().getText().toLowerCase())) {
                                    //Log.d("DRAWING", Integer.toString(cameraViewSize.getWidth()));
                                    float scaleFactorWidth = ((float) cameraViewSize.getWidth()) / (float)objectResult.getResultBitmap().getWidth();
                                    float scaleFactorHeight = ((float) cameraViewSize.getHeight()) / (float)objectResult.getResultBitmap().getHeight();
                                    object.drawOnCanvas(getApplicationContext(), canvas, scaleFactorWidth, scaleFactorHeight);
                                    Log.d("DRAWING", object.getBoundingBox().toShortString());
                                    newBoxes.add(object.getBoundingBox());
                                    //String id = object.getVisionLabel().getText();
                                }
                            }
                            boolean locked = lock.getAndSet(true);
                            if(locked){
                                boxes.clear();
                                boxes = newBoxes;
                                lock.set(false);
                            }
                            //objectResult.drawBoundingBoxes(canvas, cameraViewSize);
                        }
                        //Log.d("BOXES", boxes.toString());
                    }
                });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }
        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }
        int rotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        fritzVisionImage = FritzVisionImage.fromMediaImage(image, rotationFromCamera);
        image.close();

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        objectResult = predictor.predict(fritzVisionImage);
                        requestRender();
                        computing.set(false);
                    }
                });
    }


    /*
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            localBinder binder = (localBinder) service;
            WIRS = binder.getService();
            WIRSisBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            WIRSisBound = false;
        }
    };
    */
}
