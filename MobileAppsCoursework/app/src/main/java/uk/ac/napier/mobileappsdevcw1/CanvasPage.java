package uk.ac.napier.mobileappsdevcw1;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.stfalcon.multiimageview.MultiImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaScannerConnection.scanFile;
import static java.lang.System.out;

//=============================
//Author: Mark Pereira 40286471
//Last Modified: 15/03/18 10:35am
//=============================
//CanvasPage Activity is where you see and manage the Canvas, the main function of the App
public class CanvasPage extends AppCompatActivity {

    //Represents User control mode when tapping the canvas
    private final int MY_MODE_DRAW = 0;
    private final int MY_MODE_MOVE = 1;
    private int actor_mode = 0; //Initialized to 0 (Draw mode)
    private int imageResource = 0; //Int holding the Location of the desired canvas image

    private int selectedOverlayIndex = 0; //Selected overlay layer to move or delete

    private ImageView mImageView; //This is the background image on which to draw
    private ImageView overlayImageView; //This is a transparent layer which holds the Canvas

    private CanvasClass overlayCanvas; //This is the actual canvas which holds multiple Bitmaps

    private Bitmap mBitmap; //Holds background bitmap
    private Bitmap overlayBitmap; //Holds overlay bitmap

    private EditText messageField;//Message to display


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_page);

        setTitle("Canvas");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageResource = extras.getInt("key");
            //Gets the selected canvas image from the image browser
        }

        //Sets the background and overlay image View.  The Overlay is transparent, but has the same
        //size as the Background
        mImageView = (ImageView) findViewById(R.id.imageViewCanvas);
        overlayImageView = (ImageView) findViewById(R.id.imageViewOverlay);

        messageField = (EditText) findViewById(R.id.commentText);

        generateImage();

        //Button: Switch to move mode. When the user taps the canvas, the selected Comment window is moved there
        Button btModeMove = (Button) findViewById(R.id.BT_ModeMove);
        {
            btModeMove.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(getBaseContext(),"Switched to Move Mode",Toast.LENGTH_SHORT).show();
                   actor_mode = MY_MODE_MOVE;
                }
            });
        }

        //Button: Switch to draw mode. When the user taps the canvas, a new Comment window is drawn
        Button btModeDraw = (Button) findViewById(R.id.BT_ModeDraw);
        {
            btModeDraw.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(getBaseContext(),"Switched to Draw Mode",Toast.LENGTH_SHORT).show();
                    actor_mode = MY_MODE_DRAW;
                }
            });
        }

        //Switches the sleceted Comment Window (Which may be moved or deleted).
        Button btSwitchTarget = (Button) findViewById(R.id.BT_SwitchTarget);
        {
            btSwitchTarget.setOnClickListener(new  View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(selectedOverlayIndex  < overlayCanvas.getLayerCount()-1)
                    {
                        //Deselect current rectangle, increment selection index, select new rectangle, then update
                        overlayCanvas.deselectPrevRectangle(selectedOverlayIndex);
                        updateOverlay();

                        selectedOverlayIndex++;
                        overlayCanvas.setSelectedRectangle(selectedOverlayIndex);
                        updateOverlay();

                    }
                    else
                    {
                        //It selected the last rectangle, reset selection index to 0 and select
                        overlayCanvas.deselectPrevRectangle(selectedOverlayIndex);
                        updateOverlay();
                        selectedOverlayIndex = 0;
                        overlayCanvas.setSelectedRectangle(selectedOverlayIndex);
                        updateOverlay();
                    }
                }
            });
        }

        //Button: Delete Selected Comment Window
        Button btDeleteSelected = (Button) findViewById(R.id.BT_DeleteSelected);
        {
            btDeleteSelected.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    overlayCanvas.deleteBitmap(selectedOverlayIndex);
                    updateOverlay();
                    if(selectedOverlayIndex >= overlayCanvas.getLayerCount())
                    {
                        selectedOverlayIndex--;
                    }
                }
            });
        }


        //Button: Save Canvas Image to internal Storage. Currently not functioning
        Button btSaveImage = (Button) findViewById(R.id.BT_SaveImage);
        {

            btSaveImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(getBaseContext(),"Function not available in prototype Version",Toast.LENGTH_SHORT).show();
                    Bitmap base_bitmap = BitmapFactory.decodeResource(getResources(),imageResource);
                    overlayCanvas.deselectPrevRectangle(selectedOverlayIndex);
                    Bitmap overlay_bitmap = overlayCanvas.getMergedBitmap();

                    Bitmap final_bitmap = overlay(base_bitmap,overlay_bitmap);
                    saveImageToInternalStorage(final_bitmap);


                }
            });
        }



    }



    public boolean saveImageToInternalStorage(Bitmap image) {

        try {
            // Use the compress method on the Bitmap object to write image to
            // the OutputStream
            FileOutputStream fos = getBaseContext().openFileOutput("desiredFilename.jpg", Context.MODE_PRIVATE);

            // Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.close();


            return true;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
            return false;
        }
    }

    //Sets the initial background image
    public void generateImage()
    {
        if(imageResource != 0)
        {
            mBitmap = BitmapFactory.decodeResource(getBaseContext().getResources(),imageResource);
            mImageView.setImageBitmap(mBitmap);
            drawOverlay();
        }
    }

    //Used to update the overlay by reconstructing the collected bitmaps
    //Called after every change to the canvas
    public void updateOverlay()
    {
        overlayBitmap =  overlayCanvas.getMergedBitmap();
        overlayImageView.setImageBitmap(overlayBitmap);
    }


    //Action processing for the canvas
    public void canvasClick(View view)
    {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Converts ImageView click coordinates to Bitmap click Coordinates
                float click_x = getPointerCoords(mImageView,motionEvent)[0];
                float click_y = getPointerCoords(mImageView,motionEvent)[1];

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                         if(actor_mode == MY_MODE_DRAW)
                         {
                             //Draws a new rectangle object
                             String message = messageField.getText().toString();
                             overlayCanvas.drawRectangle(click_x,click_y,400,200,message);
                             updateOverlay();

                         }
                         if(actor_mode==MY_MODE_MOVE)
                         {
                             //Moves the selected rectangle object
                             String message = overlayCanvas.getString(selectedOverlayIndex);
                            overlayCanvas.moveBitmap(selectedOverlayIndex,click_x,click_y,message);
                             updateOverlay();

                         }
                        view.invalidate();

                        break;

                    default:
                }
                return true;
            }

        });

    }

    //This was cruicial code! Thanks to akonsu. Used to convert imageView coordinates to Bitmap coordinates
    //answered Mar 30 '12 at 15:22 akonsu
    //https://stackoverflow.com/questions/4933612/how-to-convert-coordinates-of-the-image-view-to-the-coordinates-of-the-bitmap
    final float[] getPointerCoords(ImageView view, MotionEvent e)
    {
        final int index = e.getActionIndex();
        final float[] coords = new float[] { e.getX(index), e.getY(index) };
        Matrix matrix = new Matrix();
        view.getImageMatrix().invert(matrix);
        matrix.postTranslate(view.getScrollX(), view.getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }

    //Draws the initial transparent Overlay
    public void drawOverlay()
    {
        overlayBitmap = BitmapFactory.decodeResource(getBaseContext().getResources(),imageResource);

        if(imageResource!=0){
            overlayImageView.setImageBitmap(overlayBitmap);
            Bitmap immutableBmp= BitmapFactory.decodeResource(getApplicationContext().getResources(),imageResource);
            Bitmap mutableBitmap = immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
            overlayCanvas = new CanvasClass(CanvasPage.this, mutableBitmap, this.overlayImageView);

        }

    }
    //Used to merge Bitmaps. The 'final' overlay is constructed by merging various Bitmaps into one
    //https://stackoverflow.com/questions/10616777/how-to-merge-to-two-bitmap-one-over-another
    //answered May 16 '12 at 10:41
    //RajaReddy PolamReddy
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }


}
