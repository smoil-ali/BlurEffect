package com.techyasoft.blureffect.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
        CIRCLE,RECTANGLE,HEART,STAR
    }

    private ShapeMode shapeMode = ShapeMode.CIRCLE;

    private Path mainPath, bitmapPath;
    private Paint mainPaint;




    // circle radius
    private float circleRadius = 540f;

    // rectangle width and height
    private final float rectangleWidth = 400;
    private final float rectangleHeight = 300;
    private float bitmapLeftCentre,bitmapRightCentre,bitmapTopCentre,bitmapBottomCentre;


    //heart params
    private float centreX;
    private float centreY;


    //it will adjust width
    private float xMargin;

    //it will adjust heart curve corners
    private float yStartMargin;

    // they will adjust heart height
    private float yMidMargin;
    private float yEndMargin;

    //heart start point
    private float heartStartPoint = yEndMargin / 2;

    //start shape params
    Point mainPoint = new Point();
    Point point2 = new Point(100,200);
    Point point3 = new Point(300,230);
    Point point4 = new Point(150,330);
    Point point5 = new Point(200,530);
    Point point6 = new Point(0,430);
    Point point7 = new Point(200,530);
    Point point8 = new Point(150,330);
    Point point9 = new Point(300,230);
    Point point10 = new Point(100,200);


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

        mainPath = new Path();
        bitmapPath = new Path();

        mainPaint = new Paint();
        mainPaint.setColor(ContextCompat.getColor(context, R.color.circle_stroke));
        mainPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mainPaint.setStrokeWidth(5f);

        setImageBitmap(bitmap);
        fitScreen();


    }

    public void setNormalBitmapShader(){
        Log.d(TAG, "setNormalBitmapShader: ");
        bitmapShader = new BitmapShader(orgBitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        mainPaint.setShader(bitmapShader);
        bitmapShader.setLocalMatrix(matrix);


        postInvalidate();
    }

    @Override
    void readyToGo() {
        Log.d(TAG, "getStrokeSize: resRatio "+resRatio * circleRadius);
        setNormalBitmapShader();
        showInit();
    }



    private void showInit(){

        resetBitmapCanvas();
        float maxClipHeight = (orgHeight) + transY;
        float top = 0;
        float bottom = Math.min(maxClipHeight,screenHeight);
        if (transY >= 0) {
            top = transY;
        }

        float right = (orgWidth) + transX;
        float sum = top + bottom;
        touchY = sum / 2;
        touchX = orgWidth / 2;



        float leftCentre = right / 2;
        float rightCentre = right / 2;
        float topCentre = bottom / 2;
        float bottomCentre = bottom / 2;


        bitmapLeftCentre = ((leftCentre - transX) - rectangleWidth) /finalScale;
        bitmapRightCentre = ((rightCentre - transX) + rectangleWidth) /finalScale;
        bitmapTopCentre = ((topCentre - transY) - rectangleHeight) /finalScale;
        bitmapBottomCentre = ((bottomCentre - transY) + rectangleHeight) /finalScale;


        float topBit = (top - transY) / finalScale;
        float bottomBit = (bottom - transY) / finalScale;



        mainPaint.getShader().setLocalMatrix(new Matrix());

        if (shapeMode == ShapeMode.CIRCLE){
            // fix circle radius issue
            bitmapPath.reset();
            float result = bottomBit + topBit;
            float rightBit = (right - transX) / finalScale;

            circleRadius = (float) screenWidth / 2;
            bitmapPath.moveTo(rightBit / 2, result / 2);
            bitmapPath.addCircle(rightBit / 2, result / 2,
                    circleRadius,Path.Direction.CW);

        }else if (shapeMode == ShapeMode.RECTANGLE){
            bitmapPath.reset();
            bitmapPath.moveTo(touchX,touchY);
            bitmapPath.addRect(bitmapLeftCentre,
                    bitmapTopCentre,
                    bitmapRightCentre ,
                    bitmapBottomCentre,
                    Path.Direction.CW);

        }else if (shapeMode == ShapeMode.HEART){
            centreX = right/2;
            centreY = (top + bottom)/2;


            //it will adjust width changeable
            xMargin = 300f;

            //it will adjust heart curve corners changeable
            yStartMargin = 200f;

            // it will adjust heart height changeable
            yEndMargin = 400f;
            // it will be fixed
            yMidMargin = yEndMargin - 100f;
            
            //heart start point
            heartStartPoint = yEndMargin / 2;

            bitmapPath.reset();
            bitmapPath.moveTo((centreX - transX)/finalScale,((centreY - heartStartPoint) - transY)/finalScale);
            bitmapPath.cubicTo( ((centreX - transX) - xMargin)/finalScale,
                    (((centreY - heartStartPoint) - transY) - yStartMargin)/ finalScale,
                    ((centreX - transX) - xMargin)/finalScale,
                    (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                    (centreX - transX)/finalScale,
                    (yEndMargin + ((centreY - heartStartPoint) - transY))/finalScale);
            bitmapPath.cubicTo(((centreX - transX) + xMargin)/finalScale,
                    (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                    ((centreX - transX) + xMargin)/finalScale,
                    (((centreY - heartStartPoint) - transY) - yStartMargin)/finalScale,
                    (centreX - transX)/finalScale,
                    ((centreY - heartStartPoint) - transY)/finalScale);

        }else if (shapeMode == ShapeMode.STAR){
            bitmapPath.reset();
            mainPoint.x =(int) touchX;
            mainPoint.y = (int) touchY - 400;
            bitmapPath.moveTo((mainPoint.x - transX)/finalScale ,
                    (mainPoint.y - transY)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) - point2.x)/finalScale,
                    ((mainPoint.y - transY) + point2.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) - point3.x)/finalScale,
                    ((mainPoint.y - transY) + point3.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) - point4.x)/finalScale,
                    ((mainPoint.y - transY) + point4.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) - point5.x)/finalScale,
                    ((mainPoint.y - transY) + point5.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) + point6.x)/finalScale,
                    ((mainPoint.y - transY) + point6.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) + point7.x)/finalScale,
                    ((mainPoint.y - transY) + point7.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) + point8.x)/finalScale,
                    ((mainPoint.y - transY) + point8.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) + point9.x)/finalScale,
                    ((mainPoint.y - transY) + point9.y)/finalScale);
            bitmapPath.lineTo(((mainPoint.x - transX) + point10.x)/finalScale,
                    ((mainPoint.y - transY) + point10.y)/finalScale);
            

        }



        bitmapCanvas.drawPath(bitmapPath, mainPaint);
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




        if (mainPath != null && mainPaint != null){
            canvas.drawPath(mainPath, mainPaint);
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
                mainPaint.getShader().setLocalMatrix(matrix);

                if (shapeMode == ShapeMode.CIRCLE){
                    mainPath.reset();
                    mainPath.moveTo(touchX,touchY);
                    mainPath.addCircle(touchX,touchY,
                            circleRadius * resRatio,Path.Direction.CW);
                }else if (shapeMode == ShapeMode.RECTANGLE){
                    mainPath.reset();
                    mainPath.moveTo(touchX,touchY);
                    mainPath.addRect(touchX - rectangleWidth,touchY - rectangleHeight ,
                            touchX + rectangleWidth ,touchY + rectangleHeight,
                            Path.Direction.CW);
                }else if (shapeMode == ShapeMode.HEART){
                    mainPath.reset();
                    centreX = touchX;
                    centreY = touchY;
                    mainPath.moveTo(centreX,centreY - heartStartPoint);
                    mainPath.cubicTo( centreX - xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX - xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX,
                            yEndMargin + (centreY - heartStartPoint));
                    mainPath.cubicTo(centreX + xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX + xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX,
                            centreY - heartStartPoint);
                }else if (shapeMode == ShapeMode.STAR){
                    mainPath.reset();

                    mainPoint.x =(int) touchX;
                    mainPoint.y = (int) touchY - 400;
                    mainPath.moveTo(mainPoint.x ,mainPoint.y);
                    mainPath.lineTo(mainPoint.x - point2.x,mainPoint.y + point2.y);
                    mainPath.lineTo(mainPoint.x - point3.x,mainPoint.y + point3.y);
                    mainPath.lineTo(mainPoint.x - point4.x,mainPoint.y + point4.y);
                    mainPath.lineTo(mainPoint.x - point5.x,mainPoint.y + point5.y);
                    mainPath.lineTo(mainPoint.x + point6.x,mainPoint.y + point6.y);
                    mainPath.lineTo(mainPoint.x + point7.x,mainPoint.y + point7.y);
                    mainPath.lineTo(mainPoint.x + point8.x,mainPoint.y + point8.y);
                    mainPath.lineTo(mainPoint.x + point9.x,mainPoint.y + point9.y);
                    mainPath.lineTo(mainPoint.x + point10.x,mainPoint.y + point10.y);
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (shapeMode == ShapeMode.CIRCLE){
                    mainPath.reset();
                    mainPath.moveTo(touchX,touchY);
                    mainPath.addCircle(touchX,touchY,
                            circleRadius * resRatio,Path.Direction.CW);
                }else if (shapeMode == ShapeMode.RECTANGLE){
                    mainPath.reset();
                    mainPath.moveTo(touchX,touchY);
                    mainPath.addRect(touchX - rectangleWidth,touchY - rectangleHeight ,
                            touchX + rectangleWidth ,touchY + rectangleHeight,
                            Path.Direction.CW);
                }else if (shapeMode == ShapeMode.HEART){
                    mainPath.reset();
                    centreX = touchX;
                    centreY = touchY;
                    mainPath.moveTo(centreX,centreY - heartStartPoint);
                    mainPath.cubicTo( centreX - xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX - xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX,
                            yEndMargin + (centreY - heartStartPoint));
                    mainPath.cubicTo(centreX + xMargin,
                            yMidMargin + (centreY - heartStartPoint),
                            centreX + xMargin,
                            (centreY - heartStartPoint) - yStartMargin,
                            centreX,
                            centreY - heartStartPoint);
                }else if (shapeMode == ShapeMode.STAR){
                    mainPath.reset();

                    mainPoint.x =(int) touchX;
                    mainPoint.y = (int) touchY - 400;
                    mainPath.moveTo(mainPoint.x ,mainPoint.y);
                    mainPath.lineTo(mainPoint.x - point2.x,mainPoint.y + point2.y);
                    mainPath.lineTo(mainPoint.x - point3.x,mainPoint.y + point3.y);
                    mainPath.lineTo(mainPoint.x - point4.x,mainPoint.y + point4.y);
                    mainPath.lineTo(mainPoint.x - point5.x,mainPoint.y + point5.y);
                    mainPath.lineTo(mainPoint.x + point6.x,mainPoint.y + point6.y);
                    mainPath.lineTo(mainPoint.x + point7.x,mainPoint.y + point7.y);
                    mainPath.lineTo(mainPoint.x + point8.x,mainPoint.y + point8.y);
                    mainPath.lineTo(mainPoint.x + point9.x,mainPoint.y + point9.y);
                    mainPath.lineTo(mainPoint.x + point10.x,mainPoint.y + point10.y);
                }

                break;
            case MotionEvent.ACTION_UP:
                resetBitmapCanvas();
                mainPaint.getShader().setLocalMatrix(new Matrix());
                if (shapeMode == ShapeMode.CIRCLE){
                    Log.d(TAG, "onTouchEvent: if");
                    bitmapPath.reset();
                    bitmapPath.moveTo(bitmapX,bitmapY);
                    bitmapPath.addCircle(bitmapX,bitmapY, circleRadius,Path.Direction.CW);
                }else if (shapeMode == ShapeMode.RECTANGLE){
                    bitmapPath.reset();
                    bitmapPath.moveTo(bitmapX,bitmapY);
                    bitmapLeftCentre = ((touchX - transX) - rectangleWidth) /finalScale;
                    bitmapRightCentre = ((touchX - transX) + rectangleWidth) /finalScale;
                    bitmapTopCentre = ((touchY - transY) - rectangleHeight) /finalScale;
                    bitmapBottomCentre = ((touchY - transY) + rectangleHeight) /finalScale;
                    bitmapPath.addRect(bitmapLeftCentre,
                            bitmapTopCentre ,
                            bitmapRightCentre ,
                            bitmapBottomCentre,
                            Path.Direction.CW);
                }else if (shapeMode == ShapeMode.HEART){
                    bitmapPath.reset();
                    bitmapPath.moveTo((centreX - transX)/finalScale,((centreY - heartStartPoint) - transY)/finalScale);
                    bitmapPath.cubicTo( ((centreX - transX) - xMargin)/finalScale,
                            (((centreY - heartStartPoint) - transY) - yStartMargin)/ finalScale,
                            ((centreX - transX) - xMargin)/finalScale,
                            (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                            (centreX - transX)/finalScale,
                            (yEndMargin + ((centreY - heartStartPoint) - transY))/finalScale);
                    bitmapPath.cubicTo(((centreX - transX) + xMargin)/finalScale,
                            (yMidMargin + ((centreY - heartStartPoint) - transY))/finalScale,
                            ((centreX - transX) + xMargin)/finalScale,
                            (((centreY - heartStartPoint) - transY) - yStartMargin)/finalScale,
                            (centreX - transX)/finalScale,
                            ((centreY - heartStartPoint) - transY)/finalScale);
                }else if (shapeMode == ShapeMode.STAR){
                    bitmapPath.reset();
                    // run experiment with bitmapX,bitmapY
                    mainPoint.x =(int) touchX;
                    mainPoint.y = (int) touchY - 400;
                    bitmapPath.moveTo((mainPoint.x - transX)/finalScale ,
                            (mainPoint.y - transY)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) - point2.x)/finalScale,
                            ((mainPoint.y - transY) + point2.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) - point3.x)/finalScale,
                            ((mainPoint.y - transY) + point3.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) - point4.x)/finalScale,
                            ((mainPoint.y - transY) + point4.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) - point5.x)/finalScale,
                            ((mainPoint.y - transY) + point5.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) + point6.x)/finalScale,
                            ((mainPoint.y - transY) + point6.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) + point7.x)/finalScale,
                            ((mainPoint.y - transY) + point7.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) + point8.x)/finalScale,
                            ((mainPoint.y - transY) + point8.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) + point9.x)/finalScale,
                            ((mainPoint.y - transY) + point9.y)/finalScale);
                    bitmapPath.lineTo(((mainPoint.x - transX) + point10.x)/finalScale,
                            ((mainPoint.y - transY) + point10.y)/finalScale);
                }
                mainPath.reset();
                bitmapCanvas.drawPath(bitmapPath, mainPaint);
                break;
        }

        postInvalidate();
        return true;
    }
}
