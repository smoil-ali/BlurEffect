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

import java.util.ArrayList;
import java.util.Stack;

public class BlurView extends CommonView{
    private static final String TAG = "BlurView";

    private final float OFFSET_RADIUS = 40f;
    private Paint blurDrawPaint, strokeCirclePaint,fillCirclePaint,offsetCirclePaint,
            lineOffsetPaint,normalDrawPaint;
    private Path drawPath, blurBitmapPath,touchBlurBitmapPath, strokeCirclePath,fillCirclePath,offsetCirclePath,
            lineOffsetPath, normalBitmapPath,touchNormalBitmapPath;

    float strokeWidth = 250f;

    // set half size initially
    float offsetY = 250f;
    float offsetX = 250f;

    private final ArrayList<PathModel> pathArrayList = new ArrayList<>();
    private final Stack<PathModel> undoList = new Stack<>();

    private UndoListener listener;

    private enum BlurStatus {
        BLUR_MODE_ON,BLUR_MODE_OFF
    }

    private enum DrawMode{
        VERTICAL,RIGHT_MID,LEFT_MID
    }



    private BlurStatus MODE = BlurStatus.BLUR_MODE_ON;
    private DrawMode drawMode = DrawMode.LEFT_MID;


    public BlurView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUndoListener(UndoListener listener){
        this.listener = listener;
    }


    public void init(Bitmap srcBitmap, Bitmap blurBitmap){
        orgBitmap = srcBitmap;
        orgBlurBitmap = blurBitmap;



        bitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawBitmap(srcBitmap,0,0,null);



        drawPath = new Path();
        normalBitmapPath = new Path();
        touchNormalBitmapPath = new Path();
        blurBitmapPath = new Path();
        touchBlurBitmapPath = new Path();
        strokeCirclePath = new Path();
        fillCirclePath = new Path();
        offsetCirclePath = new Path();
        lineOffsetPath = new Path();

        blurDrawPaint = new Paint();
        blurDrawPaint.setStyle(Paint.Style.STROKE);
        blurDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        blurDrawPaint.setStrokeJoin(Paint.Join.ROUND);



        normalDrawPaint = new Paint();
        normalDrawPaint.setStyle(Paint.Style.STROKE);
        normalDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        normalDrawPaint.setStrokeJoin(Paint.Join.ROUND);

        strokeCirclePaint = new Paint();
        strokeCirclePaint.setColor(ContextCompat.getColor(context, R.color.circle_stroke));
        strokeCirclePaint.setStyle(Paint.Style.STROKE);
        strokeCirclePaint.setStrokeWidth(5f);


        fillCirclePaint = new Paint();
        fillCirclePaint.setColor(ContextCompat.getColor(context, R.color.circle_fill));
        fillCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillCirclePaint.setStrokeWidth(5f);

        offsetCirclePaint = new Paint();
        offsetCirclePaint.setColor(ContextCompat.getColor(context,R.color.circle_stroke));
        offsetCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        offsetCirclePaint.setStrokeWidth(5f);

        lineOffsetPaint = new Paint();
        lineOffsetPaint.setColor(ContextCompat.getColor(context,R.color.circle_stroke));
        lineOffsetPaint.setStyle(Paint.Style.STROKE);
        lineOffsetPaint.setStrokeWidth(3f);


        setImageBitmap(bitmap);
        fitScreen();


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


        if (drawPath != null && blurDrawPaint != null){
            if (MODE == BlurStatus.BLUR_MODE_ON){
                canvas.drawPath(drawPath, blurDrawPaint);
            }else {
                canvas.drawPath(drawPath,normalDrawPaint);
            }
            canvas.drawPath(strokeCirclePath, strokeCirclePaint);
            canvas.drawPath(fillCirclePath, fillCirclePaint);
            canvas.drawPath(offsetCirclePath,offsetCirclePaint);
            canvas.drawPath(lineOffsetPath,lineOffsetPaint);
        }
    }

    private void showInit(){

        float maxClipHeight = (orgHeight) + transY;
        float top = 0;
        float bottom = Math.min(maxClipHeight,screenHeight);
        if (transY >= 0) {
            top = transY;
        }

        float sum = top + bottom;
        touchY = sum / 2;
        touchX = orgWidth / 2;




        strokeCirclePath.reset();
        strokeCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
        strokeCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),strokeWidth * resRatio/2,Path.Direction.CW);


        fillCirclePath.reset();
        fillCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
        fillCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),strokeWidth * resRatio/2,Path.Direction.CW);


        offsetCirclePath.reset();
        offsetCirclePath.moveTo(touchX,touchY);
        offsetCirclePath.addCircle(touchX,touchY,OFFSET_RADIUS * resRatio/2,Path.Direction.CW);

        lineOffsetPath.reset();
        lineOffsetPath.moveTo(touchX,touchY);
        lineOffsetPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));




        setBlurBitmapShader();
        setNormalBitmapShader();
    }

    private float getTouchXValue(float touchX){
        if (drawMode == DrawMode.VERTICAL){
            return touchX;
        }else if (drawMode == DrawMode.RIGHT_MID){
            return touchX + offsetX;
        }else {
            return touchX - offsetX;
        }
    }

    private void getBitmapXValue(float touchX){
        if (drawMode == DrawMode.VERTICAL){
            bitmapX = ((touchX - transX)) /finalScale;
        }else if (drawMode == DrawMode.RIGHT_MID){
            bitmapX = ((touchX - transX) + offsetX) /finalScale;
        }else {
            bitmapX = ((touchX - transX) - offsetX) /finalScale;
        }
    }

    private void getBitmapYValue(float touchY){
        bitmapY = ((touchY - transY) - offsetY)/finalScale;
    }

    private float getTouchYValue(float touchY){
        return touchY - offsetY;
    }

    public void setBlurBitmapShader(){
        Log.d(TAG, "setBlurBitmapShader: ");
        bitmapShader = new BitmapShader(orgBlurBitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        bitmapShader.setLocalMatrix(matrix);
        blurDrawPaint.setShader(bitmapShader);


        postInvalidate();
    }

    public void setNormalBitmapShader(){
        Log.d(TAG, "setNormalBitmapShader: ");
        bitmapShader = new BitmapShader(orgBitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        bitmapShader.setLocalMatrix(matrix);
        normalDrawPaint.setShader(bitmapShader);

        postInvalidate();
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

                if (MODE == BlurStatus.BLUR_MODE_ON){
                    touchBlurBitmapPath.reset();
                    blurDrawPaint.setStyle(Paint.Style.STROKE);
                    blurDrawPaint.getShader().setLocalMatrix(matrix);
                    blurDrawPaint.setStrokeWidth(resRatio * strokeWidth);

                    blurBitmapPath.moveTo(bitmapX,bitmapY);
                    touchBlurBitmapPath.moveTo(bitmapX,bitmapY);
                    blurBitmapPath.lineTo(bitmapX,bitmapY);
                    touchBlurBitmapPath.lineTo(bitmapX,bitmapY);
                }else {
                    touchNormalBitmapPath.reset();
                    normalDrawPaint.setStyle(Paint.Style.STROKE);
                    normalDrawPaint.getShader().setLocalMatrix(matrix);
                    normalDrawPaint.setStrokeWidth(resRatio * strokeWidth);

                    normalBitmapPath.moveTo(bitmapX,bitmapY);
                    touchNormalBitmapPath.moveTo(bitmapX,bitmapY);
                    normalBitmapPath.lineTo(bitmapX,bitmapY);
                    touchNormalBitmapPath.lineTo(bitmapX,bitmapY);
                }





                drawPath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));

                strokeCirclePath.reset();
                strokeCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
                strokeCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),
                        strokeWidth * resRatio/2,Path.Direction.CW);

                fillCirclePath.reset();
                fillCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
                fillCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),
                        strokeWidth * resRatio/2,Path.Direction.CW);

                offsetCirclePath.reset();
                offsetCirclePath.moveTo(touchX,touchY);
                offsetCirclePath.addCircle(touchX,touchY,OFFSET_RADIUS * resRatio/2,Path.Direction.CW);

                lineOffsetPath.reset();
                lineOffsetPath.moveTo(touchX,touchY);
                lineOffsetPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));

                break;
            case MotionEvent.ACTION_MOVE:

                if (MODE == BlurStatus.BLUR_MODE_ON){
                    blurBitmapPath.lineTo(bitmapX,bitmapY);
                    touchBlurBitmapPath.lineTo(bitmapX,bitmapY);
                }else {
                    normalBitmapPath.lineTo(bitmapX,bitmapY);
                    touchNormalBitmapPath.lineTo(bitmapX,bitmapY);
                }

                drawPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));


                strokeCirclePath.reset();
                strokeCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
                strokeCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),strokeWidth * resRatio/2, Path.Direction.CW);

                fillCirclePath.reset();
                fillCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
                fillCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),strokeWidth * resRatio/2,Path.Direction.CW);

                offsetCirclePath.reset();
                offsetCirclePath.moveTo(touchX,touchY);
                offsetCirclePath.addCircle(touchX,touchY,OFFSET_RADIUS * resRatio/2,Path.Direction.CW);

                lineOffsetPath.reset();
                lineOffsetPath.moveTo(touchX,touchY);
                lineOffsetPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));

                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));

                if (MODE == BlurStatus.BLUR_MODE_ON){
                    blurBitmapPath.lineTo(bitmapX,bitmapY);
                    pathArrayList.add(new PathModel(blurBitmapPath, blurDrawPaint));
                    undoList.push(new PathModel(blurBitmapPath,blurDrawPaint));

                    touchNormalBitmapPath.lineTo(bitmapX,bitmapY);

                    blurDrawPaint.setStrokeWidth(strokeWidth);
                    blurDrawPaint.getShader().setLocalMatrix(new Matrix());
                    bitmapCanvas.drawPath(touchBlurBitmapPath, blurDrawPaint);
                    blurBitmapPath = new Path();


                }else {
                    normalBitmapPath.lineTo(bitmapX,bitmapY);
                    pathArrayList.add(new PathModel(normalBitmapPath, normalDrawPaint));
                    undoList.push(new PathModel(normalBitmapPath,normalDrawPaint));


                    touchBlurBitmapPath.lineTo(bitmapX,bitmapY);

                    normalDrawPaint.setStrokeWidth(strokeWidth);
                    normalDrawPaint.getShader().setLocalMatrix(new Matrix());
                    bitmapCanvas.drawPath(touchNormalBitmapPath, normalDrawPaint);
                    normalBitmapPath = new Path();
                }


                undoChange();
                drawPath.reset();
                break;
        }
        postInvalidate();
        return true;
    }

    private void undoChange(){
        listener.onUndo(undoList.size());
    }

    public void blurModeOn(){
        MODE = BlurStatus.BLUR_MODE_ON;
    }

    public void blurModeOff(){
        MODE = BlurStatus.BLUR_MODE_OFF;
    }

    public void drawVerticalMode(){
        drawMode = DrawMode.VERTICAL;
    }

    public void drawRightMidMode(){
        drawMode = DrawMode.RIGHT_MID;
    }

    public void drawLeftMidMode(){
        drawMode = DrawMode.LEFT_MID;
    }


    public void setNormalImage(){


        pathArrayList.clear();
        for(int i = (int) orgTopBitmap; i<orgBottomBitmap; i+=10){
            normalBitmapPath.moveTo(orgLeftBitmap,i);
            normalBitmapPath.quadTo(orgLeftBitmap,i,orgRightBitmap,i);
        }


        normalDrawPaint.setStyle(Paint.Style.STROKE);
        normalDrawPaint.setStrokeWidth(strokeWidth);
        normalDrawPaint.getShader().setLocalMatrix(new Matrix());
        bitmapCanvas.drawPath(normalBitmapPath, normalDrawPaint);

        pathArrayList.add(new PathModel(normalBitmapPath, normalDrawPaint));
        undoList.push(new PathModel(normalBitmapPath, normalDrawPaint));
        normalBitmapPath = new Path();
        undoChange();



        MODE = BlurStatus.BLUR_MODE_ON;


        postInvalidate();





    }



    public void setBlurImage(){

        pathArrayList.clear();
        for(int i = (int) orgTopBitmap; i<orgBottomBitmap; i+=10){
            blurBitmapPath.moveTo(orgLeftBitmap,i);
            blurBitmapPath.quadTo(orgLeftBitmap,i,orgRightBitmap,i);
        }


        blurDrawPaint.setStyle(Paint.Style.STROKE);
        blurDrawPaint.setStrokeWidth(strokeWidth);
        blurDrawPaint.getShader().setLocalMatrix(new Matrix());
        bitmapCanvas.drawPath(blurBitmapPath, blurDrawPaint);


        pathArrayList.add(new PathModel(blurBitmapPath, blurDrawPaint));
        undoList.push(new PathModel(blurBitmapPath,blurDrawPaint));
        blurBitmapPath = new Path();
        undoChange();

        MODE = BlurStatus.BLUR_MODE_OFF;
        postInvalidate();




    }

    public void changeDensity(Bitmap srcBitmap){

        orgBlurBitmap = srcBitmap;
        setBlurBitmapShader();
        bitmapCanvas.drawBitmap(orgBitmap,0,0,null);

        for (int i=0;i<pathArrayList.size();i++){
            PathModel pathModel = pathArrayList.get(i);
            pathModel.getPaint().setStrokeWidth(strokeWidth);
            pathModel.getPaint().getShader().setLocalMatrix(new Matrix());
            bitmapCanvas.drawPath(pathModel.getPath(),pathModel.getPaint());
        }


        invalidate();


    }

    public void undo(){

        Log.d(TAG, "undo: "+undoList.size());
        if (undoList.size() == 0)
            return;
        undoList.pop();
        Log.d(TAG, "undo: after popup "+undoList.size());
        if (undoList.size() > 0){
            resetBitmapCanvas();
            for (int i=0;i<undoList.size();i++){
                PathModel pathModel = undoList.get(i);
                Log.d(TAG, "undo: for "+undoList.size());
                pathModel.getPaint().setStrokeWidth(strokeWidth);
                pathModel.getPaint().getShader().setLocalMatrix(new Matrix());
                bitmapCanvas.drawPath(pathModel.getPath(),pathModel.getPaint());
                pathArrayList.add(new PathModel(pathModel.getPath(), pathModel.getPaint()));
            }
        }else {
            resetBitmapCanvas();
        }
        undoChange();
        postInvalidate();
    }

    private void resetBitmapCanvas(){
        bitmapCanvas.drawBitmap(orgBitmap,0,0,null);
        pathArrayList.clear();
    }

    public void changeCircleSize(int radius){
        strokeWidth = radius;

        strokeCirclePath.reset();
        strokeCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
        strokeCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),
                strokeWidth * resRatio/2,Path.Direction.CW);

        fillCirclePath.reset();
        fillCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
        fillCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),
                strokeWidth * resRatio/2,Path.Direction.CW);


        lineOffsetPath.reset();
        lineOffsetPath.moveTo(touchX,touchY);
        lineOffsetPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));

        invalidate();
    }

    public void changeOffset(int distance){
        offsetY = distance;
        offsetX = distance;

        strokeCirclePath.reset();
        strokeCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
        strokeCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),strokeWidth * resRatio/2,Path.Direction.CW);

        fillCirclePath.reset();
        fillCirclePath.moveTo(getTouchXValue(touchX),getTouchYValue(touchY));
        fillCirclePath.addCircle(getTouchXValue(touchX),getTouchYValue(touchY),strokeWidth * resRatio/2,Path.Direction.CW);

        lineOffsetPath.reset();
        lineOffsetPath.moveTo(touchX,touchY);
        lineOffsetPath.lineTo(getTouchXValue(touchX),getTouchYValue(touchY));

        invalidate();
    }

    @Override
    void readyToGo() {
        Log.d(TAG, "getStrokeSize: resRatio "+resRatio * strokeWidth);
        blurDrawPaint.setStrokeWidth(resRatio * strokeWidth);
        showInit();
    }

    private static class PathModel{
        private final Path path;
        private final Paint paint;

        public PathModel(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }

        public Path getPath() {
            return path;
        }

        public Paint getPaint() {
            return paint;
        }
    }

    public interface UndoListener{
        void onUndo(int size);
    }
}
