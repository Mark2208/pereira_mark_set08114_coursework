package uk.ac.napier.mobileappsdevcw1;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


//=============================
//Author: Mark Pereira 40286471
//Last Modified: 15/03/18 10:53am
//=============================
//Canvas Class is the Canvas which holds Overlay Bitmaps. The Overlay ImageView gets bitmaps from this Class
public class CanvasClass extends Canvas {

    private Context baseContext;

    private List<Bitmap> bitmapList = new LinkedList<Bitmap>(); //Holds individual rectangle bitmaps
    private Bitmap mBitmap;//Empty bitmap representing the Canvas Base. The bitmapList will be merged into mBitmap on update
    private Bitmap selectionBitmap; //Represents the selection Rectangle. Only 1 on the Canvas

    //Hashmap that contains rect data.
    private HashMap<Integer,Rect> rectHashMap = new HashMap<Integer, Rect>(); //This is used to reconstruct a rectangle every time it is moved
    private HashMap<Integer,String> stringHashMap = new HashMap<>(); //This is used to reconstruct the rectangle message

    //Colours for rectangles and text
    private int mColorBackground;
    private int mColorRectangle;
    private int mColorSelection;
    private Paint mPaintText = new Paint();
    private Paint mPaint = new Paint();

    private Bitmap EMPTY_BITMAP; //Empty bitmap constant



    protected CanvasClass(Context context, Bitmap baseBitmap, ImageView baseImageView) {
        super(baseBitmap);
        this.baseContext=context;

        mColorRectangle = ResourcesCompat.getColor (context.getResources(), android.R.color.white,null);
        mColorBackground = ResourcesCompat.getColor(context.getResources(),android.R.color.black,null);
        mColorSelection = ResourcesCompat.getColor(context.getResources(),R.color.selection_blue,null);

        setSize(baseBitmap);

    }

    //Gets the bitmap at the selected array index. If index is empty, returns an empty bitmap
    public Bitmap getBitmap(int index)
    {
        if(bitmapList.isEmpty() || bitmapList.get(index)==null)
        {
            return bitmapList.get(index);
        }

        return Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
    }

    //Draws a selection rectangle around the selected indexed bitmap
    public void setSelectedRectangle(int index)
    {

        selectionBitmap = drawSelectionRect(rectHashMap.get(index));
    }

    //Removes the selection rectangle bitmap after the base rectangle is moved or deleted
    public void deselectPrevRectangle(int index)
    {
        selectionBitmap = clearBitmap(selectionBitmap);
    }


    //Renders the canvas bitmap
    public Bitmap getMergedBitmap()
    {
        //Empties the bitmap, so it can be reconstructed
        mBitmap = clearBitmap(mBitmap);
        //Set render target
        this.setBitmap(mBitmap);
        //Construct bitmap
        for(Bitmap thisBitmap:bitmapList)
        {
            mBitmap = overlay(mBitmap,thisBitmap);
        }
        //Add selection rect, if available
        mBitmap = overlay(mBitmap,selectionBitmap);

        return mBitmap;
    }

    //Clears the parsed bitmap
    public Bitmap clearBitmap(Bitmap bitmap)
    {
        bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        return bitmap;
    }


    //Returns number of bitmaps to switch between
    public int getLayerCount()
    {
        return bitmapList.size();
    }


    //Sets a base bitmap image as the canvas params, since it contains the size
    public void setSize(Bitmap baseBitmap)
    {
        EMPTY_BITMAP= Bitmap.createBitmap(baseBitmap.getWidth(), baseBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        selectionBitmap=EMPTY_BITMAP;
        mBitmap = Bitmap.createBitmap(baseBitmap.getWidth(), baseBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        this.setBitmap(mBitmap);
    }

    //Conmmand to delete a rectangle from the bitmap list
    public void deleteBitmap(int index)
    {
        if(!bitmapList.isEmpty())
        {
            deselectPrevRectangle(index);
            bitmapList.remove(index);

        }


    }

    //Assuming bitmap is always a rectangle (which for now, it is)
    //Rather than 'moving' the bitmap, it is reconstructed by locating the previous rectangle in the hashmap
    public void moveBitmap(int index, float new_x , float new_y, String message)
    {

        if(bitmapList.isEmpty()) {return;}
        bitmapList.set(index,reconstructRect(rectHashMap.get(index),new_x,new_y,message,index));
        deselectPrevRectangle(index);
        setSelectedRectangle(index);

    }

    //Used to construct a new rectangle bitmap after it has been moved
    public Bitmap reconstructRect(Rect backRect,float new_x , float new_y, String message,int index)
    {
        //Change in x & y
        int delta_x = (int)new_x - backRect.left;
        int delta_y = (int)new_y - backRect.top;

        double width = backRect.right - backRect.left;
        double height = backRect.top - backRect.bottom;

        backRect.set((int)new_x,(int)new_y,backRect.right + delta_x,backRect.bottom + delta_y);

        Bitmap rectBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        this.setBitmap(rectBitmap);

        mPaint.setColor(mColorBackground);
        drawRect(backRect, mPaint);
        drawInnerRect(backRect);

        double textOffset_X = new_x + 0.03*(width);
        double textOffsetY = new_y - 0.7*(height);
        drawText(message, (float)textOffset_X, (float) textOffsetY, mPaintText);

        //Update the rect hash map
        rectHashMap.put(index,backRect);

        return rectBitmap;


    }


    //Draws a new Rectangle
    public void drawRectangle(float x, float y,int width, int height, String message)
    {
        if(bitmapList.size()>=5){
            Toast.makeText(this.baseContext,"Max draw limit reached!",Toast.LENGTH_SHORT).show();
            return;};

        Log.i("0666","Created Bitmap");
        //Creates an empty bitmap the size of the canvas
        Bitmap rectBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        this.setBitmap(rectBitmap); //Sets the draw target to the empty rect bitmap

        //Draw Rectangle
        int intx  = (int)x;
        int inty = (int)y;
        Rect backRect = new Rect();
        backRect.set(intx ,inty, intx + width,inty - height);
        mPaint.setColor(mColorBackground);
        drawRect(backRect, mPaint);
        //Draw Inner Rect
        drawInnerRect(backRect);

        mPaintText.setColor(ResourcesCompat.getColor(baseContext.getResources(), R.color.solid_black, null));
        mPaintText.setTextSize(50);
        double textOffset_X = intx + 0.03*(width);
        double textOffsetY = inty - 0.7*(height);
        drawText(message, (float)textOffset_X, (float) textOffsetY, mPaintText);

        //Adds the new bitmap to the Bitmap list to render
        bitmapList.add(rectBitmap);
        //Adds the rectangle data to the hashmap, to be recovered when moving
        stringHashMap.put(bitmapList.size()-1,message);
        rectHashMap.put(bitmapList.size()-1,backRect);

    }

    //Inner Rect is the White Rectangle, drawn after the black outer Recr
    public void drawInnerRect(Rect rect)
    {

        double offset = ((rect.right - rect.left) * 0.02);
        int iOffest = (int)(Math.round(offset));
       Rect InnerRect = new Rect( rect.left + iOffest,rect.top - iOffest, rect.right-iOffest,rect.bottom+iOffest);
        mPaint.setColor(mColorRectangle);
        drawRect(InnerRect, mPaint);


    }


    //Draws the blue selection rectangle
    public Bitmap drawSelectionRect(Rect rect)
    {

        setBitmap(selectionBitmap);
        double offset = ((rect.right - rect.left) * 0.02);
        int iOffest = (int)(Math.round(offset));
        //Selection rect is slightly bigger than the base rect
        Rect selectionRect = new Rect( rect.left - iOffest,rect.top + iOffest, rect.right+iOffest,rect.bottom-iOffest);
        mPaint.setColor(mColorSelection);
        mPaint.setAlpha(75);
        drawRect(selectionRect, mPaint);
        mPaint.setAlpha(255);
        setBitmap(mBitmap);
        return  selectionBitmap;


    }

    //Returns the message from the selected bitmap index message
    public String getString(int index)
    {
        return stringHashMap.get(index);
    }


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
