package com.example.grab4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opencv.core.Point;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_OPEN_IMAGE = 1;

    String mCurrentPhotoPath;
    Bitmap mBitmap;
    ImageView mImageView;
    int touchCount = 0;
    Point tl;
    Point br;
    boolean targetChose = false;
    ProgressDialog dlg;
    Button select_img;
    Button select_area;
    Button processing;
    RelativeLayout temp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imgDisplay);
//        select_img = (Button) findViewById(R.id.select_img);
//        select_area = (Button) findViewById(R.id.select_area);
//        processing = (Button) findViewById(R.id.processing);
        temp = (RelativeLayout) findViewById(R.id.main);
        dlg = new ProgressDialog(this);
        tl = new Point();
        br = new Point();
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error

        }
//        select_img.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                getPictureIntent.setType("image/*");
//                Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {
//                        pickPictureIntent
//                });
//                startActivityForResult(chooserIntent, REQUEST_OPEN_IMAGE);
//            }
//        });
//        select_area.setOnClickListener(new View.OnClickListener(){
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public void onClick(View v) {
//                mImageView.setOnTouchListener(new View.OnTouchListener() {
//
//                    @SuppressLint("ClickableViewAccessibility")
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                            if (touchCount == 0) {
//                                tl.x = event.getX();
//                                Log.d("fuckx", String.valueOf(tl.x));
//                                Log.d("fucky", String.valueOf(tl.y));
//                                tl.y = event.getY();
//                                touchCount++;
//                            }
//                            else if (touchCount == 1) {
//                                br.x = event.getX();
//                                br.y = event.getY();
//                                Log.d("fuckx", String.valueOf(br.x));
//                                Log.d("fucky", String.valueOf(br.y));
//                                Paint rectPaint = new Paint();
//                                rectPaint.setARGB(255, 255, 0, 0);
//                                rectPaint.setStyle(Paint.Style.STROKE);
//                                rectPaint.setStrokeWidth(3);
//                                Bitmap tmpBm = Bitmap.createBitmap(mBitmap.getWidth(),
//                                        mBitmap.getHeight(), Bitmap.Config.RGB_565);
//                                Canvas tmpCanvas = new Canvas(tmpBm);
//
//                                tmpCanvas.drawBitmap(mBitmap, 0, 0, null);
//                                tmpCanvas.drawRect(new RectF((float) tl.x, (float) tl.y, (float) br.x, (float) br.y),
//                                        rectPaint);
//                                mImageView.setImageDrawable(new BitmapDrawable(getResources(), tmpBm));
//
//                                targetChose = true;
//                                touchCount = 0;
//                                mImageView.setOnTouchListener(null);
//                            }
//                        }
//
//                        return true;
//                    }
//                });
//
//
//            }
//        });
//        processing.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                if (mCurrentPhotoPath != null && targetChose) {
//                    new ProcessImageTask().execute();
//                    targetChose = false;
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setPic() {
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 1;
        bmOptions.inPurgeable = true;

        mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(mBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri imgUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                assert imgUri != null;
                Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
                        null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                mCurrentPhotoPath = cursor.getString(colIndex);
                cursor.close();
                setPic();
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_open_img:
                Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getPictureIntent.setType("image/*");
                Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{
                        pickPictureIntent
                });
                startActivityForResult(chooserIntent, REQUEST_OPEN_IMAGE);
                return true;
            case R.id.action_choose_target:
                if (mCurrentPhotoPath != null)
                    targetChose = false;
                mImageView.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (touchCount == 0) {
                                tl.x = event.getX();
                                tl.y = event.getY();

                                touchCount++;
                            } else if (touchCount == 1) {
                                br.x = event.getX();
                                br.y = event.getY();

                                Paint rectPaint = new Paint();
                                rectPaint.setARGB(255, 255, 0, 0);
                                rectPaint.setStyle(Paint.Style.STROKE);
                                rectPaint.setStrokeWidth(3);
                                Bitmap tmpBm = Bitmap.createBitmap(mBitmap.getWidth(),
                                        mBitmap.getHeight(), Bitmap.Config.RGB_565);
                                Canvas tmpCanvas = new Canvas(tmpBm);

                                tmpCanvas.drawBitmap(mBitmap, 0, 0, null);
                                tmpCanvas.drawRect(new RectF((float) tl.x, (float) tl.y, (float) br.x, (float) br.y),
                                        rectPaint);
                                mImageView.setImageDrawable(new BitmapDrawable(getResources(), tmpBm));

                                targetChose = true;
                                touchCount = 0;
                                mImageView.setOnTouchListener(null);
                            }
                        }

                        return true;
                    }
                });

                return true;
            case R.id.action_cut_image:
                if (mCurrentPhotoPath != null && targetChose) {
                    new ProcessImageTask().execute();
                    targetChose = false;
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ProcessImageTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlg.setMessage("Processing Image...");
            dlg.setCancelable(false);
            dlg.setIndeterminate(true);
            dlg.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Mat img = Imgcodecs.imread(mCurrentPhotoPath);
            Mat background = new Mat(img.size(), CvType.CV_8UC3,
                    new Scalar(255, 255, 255));
            Mat firstMask = new Mat();
            Mat bgModel = new Mat();
            Mat fgModel = new Mat();
            Mat mask;
            Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
            Mat dst = new Mat();
            Rect rect = new Rect(tl, br);

            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel,
                    5, Imgproc.GC_INIT_WITH_RECT);
            Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

            Mat foreground = new Mat(img.size(), CvType.CV_8UC3,
                    new Scalar(255, 255, 255));
            img.copyTo(foreground, firstMask);

            Scalar color = new Scalar(255, 0, 0, 255);
            Imgproc.rectangle(img, tl, br, color);

            Mat tmp = new Mat();
            Imgproc.resize(background, tmp, img.size());
            background = tmp;
            mask = new Mat(foreground.size(), CvType.CV_8UC1,
                    new Scalar(255, 255, 255));

            Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY_INV);
            System.out.println();
            Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
            background.copyTo(dst);

            background.setTo(vals, mask);

            Core.add(background, foreground, dst, mask);

            firstMask.release();
            source.release();
            bgModel.release();
            fgModel.release();
            vals.release();

            Imgcodecs.imwrite(mCurrentPhotoPath + ".png", dst);

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            Bitmap jpg = BitmapFactory
                    .decodeFile(mCurrentPhotoPath + ".png");

            mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mImageView.setAdjustViewBounds(true);
            mImageView.setPadding(2, 2, 2, 2);
            mImageView.setImageBitmap(jpg);
            mImageView.invalidate();

            dlg.dismiss();
        }
    }


}
