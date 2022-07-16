package com.techyasoft.blureffect.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.techyasoft.blureffect.R;

public class ShapeBlurView extends CommonView{
    private static final String TAG = "ShapeBlurView";

    public enum ShapeMode{
        CIRCLE,RECTANGLE,HEART
    }

    private ShapeMode shapeMode = ShapeMode.HEART;

    Path circlePath,bitmapCirclePath;
    Paint circlePaint;


    float leftCentre,rightCentre,topCentre,bottomCentre;
    float bitmapLeftCentre,bitmapRightCentre,bitmapTopCentre,bitmapBottomCentre;

    // circle radius
    float strokeWidth = 540f;

    // rectangle width and height
    float rectangleWidth = 400;
    float rectangleHeight = 300;


    //heart params
    float centreX;
    float centreY;


    //it will adjust width
    float xMargin;

    //it will adjust heart curve corners
    float yStartMargin;

    // they will adjust heart height
    float yMidMargin;
    float yEndMargin;

    //heart start point
    float heartStartPoint = yEndMargin / 2;


    public ShapeBlurView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void init(Bitmap srcBitmap, Bitmap blurBitmap){
        orgBitmap = srcBitmap;
        orgBlurBitmap = blurBitmap;

        bitmap = Bitmap.createBitmap(blurBitmap.getWidth(),blurBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawBitmap(blurBitmap,0,0,null);

        circlePath = new Path();
        bitmapCirclePath = new Path();

        circlePaint = new Paint();
        circlePaint.setColor(ContextCompat.getColor(context, R.color.circle_stroke));
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint.setStrokeWidth(5f);

        setImageBitmap(bitmap);
        fitScreen();


    }

    public void setNormalBitmapShader(){
        Log.d(TAG, "setNormalBitmapShader: ");
        bitmapShader = new BitmapShader(orgBitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        circlePaint.setShader(bitmapShader);
        bitmapShader.setLocalMatrix(matrix);


        postInvalidate();
    }

    @Override
    void readyToGo() {
        Log.d(TAG, "getStrokeSize: resRatio "+resRatio * strokeWidth);
        setNormalBitmapShader();
        showInit();
    }



    private void showInit(){

        float maxClipHeight = (orgHeight) + transY;
        float top = 0;
        float left = transX;
        float bottom = Math.min(maxClipHeight,screenHeight);
        if (transY >= 0) {
            top = transY;
        }

        float right = (orgWidth) + transX;
        float sum = top + bottom;
        touchY = sum / 2;
        touchX = orgWidth / 2;
        float tBottom = Math.min(maxClipHeight,screenHeight);


        leftCentre = right / 2;
        rightCentre = right / 2;
        topCentre = tBottom / 2;
        bottomCentre = tBottom / 2;

        bitmapLeftCentre = ((leftCentre - transX) - rectangleWidth) /finalScale;
        bitmapRightCentre = ((rightCentre - transX) + rectangleWidth) /finalScale;
        bitmapTopCentre = ((topCentre - transY) - rectangleHeight) /finalScale;
        bitmapBottomCentre = ((bottomCentre - transY) + rectangleHeight) /finalScale;


        float topBit = (top - transY) / finalScale;
        float bottomBit = (tBottom - transY) / finalScale;



        circlePaint.getShader().setLocalMatrix(new Matrix());

        if (shapeMode == ShapeMode.CIRCLE){
            // fix circle radius issue
            bitmapCirclePath.reset();
            float result = bottomBit + topBit;
            float rightBit = (right - transX) / finalScale;

            strokeWidth = (float) screenWidth / 2;
            bitmapCirclePath.moveTo(rightBit / 2, result / 2);
            bitmapCirclePath.addCircle(rightBit / 2, result / 2,
                    strokeWidth,Path.Direction.CW);

        }else if (shapeMode == ShapeMode.RECTANGLE){
            bitmapCirclePath.reset();
            bitmapCirclePath.moveTo(touchX,touchY);
            bitmapCirclePath.addRect(bitmapLeftCentre,
                    bitmapTopCentre,
                    bitmapRightCentre ,
                    bitmapBottomCentre,
                    Path.Direction.CW);

        }else if (shapeMode == ShapeMode.HEART){




            centreX = right/2;
            centreY = (top + tBottom)/2;


            //it will adjust width
            xMargin = 300f;

            //it will adjust heart curve corners
            yStartMargin = 200f;

            // they will adjust heart height
            yMidMargin = 300f;
            yEndMargin = 400f;
            
            //heart start point
            heartStartPoint = yEndMargin / 2;

            bitmapCirclePath.reset();
            bitmapCirclePath.moveTo((centreX - transX)/finalScale,((centreY - heartStartPoint) - transY)/finalScale);
            bitmapCirclePath.cubicTo( ((centreX - transX) - xMargin)/finalScale,
                    (((centreY - heartStartPoint) - transY) - yStartMargin)/ finalScale,
                    ((centreX - transX) - xMargin)/finalScale,
                    (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                    (centreX - transX)/finalScale,
                    (yEndMargin + ((centreY - heartStartPoint) - transY))/finalScale);
            bitmapCirclePath.cubicTo(((centreX - transX) + xMargin)/finalScale,
                    (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                    ((centreX - transX) + xMargin)/finalScale,
                    (((centreY - heartStartPoint) - transY) - yStartMargin)/finalScale,
                    (centreX - transX)/finalScale,
                    ((centreY - heartStartPoint) - transY)/finalScale);

        }



        bitmapCanvas.drawPath(bitmapCirclePath,circlePaint);


        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float maxClipHeight = (orgHeight) + transY;
        orgTop = 0;
        orgLeft = transX;
        if (transY >= 0) {
            orgTop = transY;
            canvas.clipRect(orgLeft,orgTop,orgRight,orgBottom, Region.Op.INTERSECT);
        }else {
            canvas.clipRect(orgLeft,orgTop,orgRight,orgBottom, Region.Op.DIFFERENCE);
        }
        orgRight = (orgWidth) + transX;
        orgBottom = Math.min(maxClipHeight,screenHeight);

        orgLeftBitmap = (orgLeft - transX) / finalScale;
        orgRightBitmap = (orgRight - transX) / finalScale;
        orgTopBitmap = (orgTop - transY) / finalScale;
        orgBottomBitmap = (orgBottom - transY) / finalScale;




        if (circlePath != null && circlePaint != null){
            canvas.drawPath(circlePath, circlePaint);
        }
    }

    private void getBitmapXValue(float touchX){
        bitmapX = ((touchX - transX)) /finalScale;
    }

    private void getBitmapYValue(float touchY){
        bitmapY = ((touchY - transY))/finalScale;
    }


    private void resetBitmapCanvas(){
        bitmapCanvas.drawBitmap(orgBlurBitmap,0,0,null);
    }

    public void changeShapeMode(ShapeMode shapeMode){
        this.shapeMode = shapeMode;
        showInit();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();
        getBitmapXValue(touchX);
        getBitmapYValue(touchY);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (touchY < orgTop || touchY > orgBottom){
                    return false;
                }
                if (touchX < orgLeft || touchX > orgRight){
                    return false;
                }

                resetBitmapCanvas();
                circlePaint.getShader().setLocalMatrix(matrix);

                if (shapeMode == ShapeMode.CIRCLE){
                    circlePath.reset();
                    circlePath.moveTo(touchX,touchY);
                    circlePath.addCircle(touchX,touchY,
                            strokeWidth * resRatio,Path.Direction.CW);
                }else if (shapeMode == ShapeMode.RECTANGLE){
                    circlePath.reset();
                    circlePath.moveTo(touchX,touchY);
                    circlePath.addRect(touchX - rectangleWidth,touchY - rectangleHeight ,
                            touchX + rectangleWidth ,touchY + rectangleHeight,
                            Path.Direction.CW);
                }else if (shapeMode == ShapeMode.HEART){
                    circlePath.reset();
                    centreX = touchX;
                    centreY = touchY;
                    circlePath.moveTo(centreX,centreY - heartStartPoint);
                    circlePath.cubicTo( centreX - xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX - xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX,
                            yEndMargin + (centreY - heartStartPoint));
                    circlePath.cubicTo(centreX + xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX + xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX,
                            centreY - heartStartPoint);
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (shapeMode == ShapeMode.CIRCLE){
                    circlePath.reset();
                    circlePath.moveTo(touchX,touchY);
                    circlePath.addCircle(touchX,touchY,
                            strokeWidth * resRatio,Path.Direction.CW);
                }else if (shapeMode == ShapeMode.RECTANGLE){
                    circlePath.reset();
                    circlePath.moveTo(touchX,touchY);
                    circlePath.addRect(touchX - rectangleWidth,touchY - rectangleHeight ,
                            touchX + rectangleWidth ,touchY + rectangleHeight,
                            Path.Direction.CW);
                }else if (shapeMode == ShapeMode.HEART){
                    circlePath.reset();
                    centreX = touchX;
                    centreY = touchY;
                    circlePath.moveTo(centreX,centreY - heartStartPoint);
                    circlePath.cubicTo( centreX - xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX - xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX,
                            yEndMargin + (centreY - heartStartPoint));
                    circlePath.cubicTo(centreX + xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX + xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX,
                            centreY - heartStartPoint);
                }

                break;
            case MotionEvent.ACTION_UP:
                resetBitmapCanvas();
                circlePaint.getShader().setLocalMatrix(new Matrix());
                if (shapeMode == ShapeMode.CIRCLE){
                    Log.d(TAG, "onTouchEvent: if");
                    bitmapCirclePath.reset();
                    bitmapCirclePath.moveTo(bitmapX,bitmapY);
                    bitmapCirclePath.addCircle(bitmapX,bitmapY,strokeWidth,Path.Direction.CW);
                }else if (shapeMode == ShapeMode.RECTANGLE){
                    bitmapCirclePath.reset();
                    bitmapCirclePath.moveTo(bitmapX,bitmapY);
                    bitmapLeftCentre = ((touchX - transX) - rectangleWidth) /finalScale;
                    bitmapRightCentre = ((touchX - transX) + rectangleWidth) /finalScale;
                    bitmapTopCentre = ((touchY - transY) - rectangleHeight) /finalScale;
                    bitmapBottomCentre = ((touchY - transY) + rectangleHeight) /finalScale;
                    bitmapCirclePath.addRect(bitmapLeftCentre,
                            bitmapTopCentre ,
                            bitmapRightCentre ,
                            bitmapBottomCentre,
                            Path.Direction.CW);
                }else if (shapeMode == ShapeMode.HEART){
                    bitmapCirclePath.reset();
                    bitmapCirclePath.moveTo((centreX - transX)/finalScale,((centreY - heartStartPoint) - transY)/finalScale);
                    bitmapCirclePath.cubicTo( ((centreX - transX) - xMargin)/finalScale,
                            (((centreY - heartStartPoint) - transY) - yStartMargin)/ finalScale,
                            ((centreX - transX) - xMargin)/finalScale,
                            (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                            (centreX - transX)/finalScale,
                            (yEndMargin + ((centreY - heartStartPoint) - transY))/finalScale);
                    bitmapCirclePath.cubicTo(((centreX - transX) + xMargin)/finalScale,
                            (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                            ((centreX - transX) + xMargin)/finalScale,
                            (((centreY - heartStartPoint) - transY) - yStartMargin)/finalScale,
                            (centreX - transX)/finalScale,
                            ((centreY - heartStartPoint) - transY)/finalScale);
                }



                circlePath.reset();
                bitmapCanvas.drawPath(bitmapCirclePath,circlePaint);
                break;
        }

        postInvalidate();
        return true;
    }
}
