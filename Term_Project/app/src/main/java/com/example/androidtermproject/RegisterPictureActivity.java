package com.example.androidtermproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterPictureActivity extends AppCompatActivity {

    private Uri mImageCaptureUri;
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private ImageView iv_UserPhoto;
    private String absolutePath;
    private ProgressDialog uploading;
    private File file;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    Uri temp;
    Uri temp_getData;
    String mCurrentPhotoPath;
    Point tl;
    Point br;
    ImageView mImageView;
    Bitmap bitmap;
    ProgressDialog dlg;
    String photoPath;

    //카메라
    public void capture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.androidtermproject.fileProvider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
                Toast.makeText(getApplicationContext(), "phtoURL : " + photoURI, Toast.LENGTH_LONG).show();
            }
        }
    }

    //앨범
    public void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);

    }

    /////////////////////////////////// onRequestPermissionResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }

    }

    String name_str;
    String category_str;
    String detail_str;
    String color_str;
    String style_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pictuer);
        mImageView = findViewById(R.id.picture_cloth);
        dlg = new ProgressDialog(this);
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error

        }
        //권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }


        final Spinner category1 = (Spinner) findViewById(R.id.category1);
        final Spinner category2 = (Spinner) findViewById(R.id.category2);
        final Spinner color = (Spinner) findViewById(R.id.color);
        final Spinner style = (Spinner) findViewById(R.id.style);

        final EditText name = (EditText) findViewById(R.id.cloth_name);

        final String[] cloth_category1 = getResources().getStringArray(R.array.cloth_category1);
        final String[] cloth_top = getResources().getStringArray(R.array.cloth_top);
        final String[] cloth_outer = getResources().getStringArray(R.array.cloth_top_outer);
        final String[] cloth_pants = getResources().getStringArray(R.array.cloth_pants);
        final String[] shoes = getResources().getStringArray(R.array.shoes);
        final String[] colors = getResources().getStringArray(R.array.color);
        final String[] styles = getResources().getStringArray(R.array.style);
        final String[] not_selected = getResources().getStringArray(R.array.not_selected);


        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.select_picture);
        linearLayout.setVisibility(View.INVISIBLE);

        Button register_picture = (Button) findViewById(R.id.register_picture);


        register_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.container);


            }
        });


        ArrayAdapter<String> category1_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, cloth_category1);
        category1_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        category1.setAdapter(category1_adapter);
        category1.setPrompt("옷중류");

        category1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String top_str = "상의";
                String pants_str = "하의";
                String shoes_str = "신발";
                String outer_str = "아우터";
                String not_selected_str = "(선택)";

                if (top_str.equals(category1.getSelectedItem().toString())) {
                    //상의
                    ArrayAdapter<String> cloth_top_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, cloth_top);
                    cloth_top_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    category2.setAdapter(cloth_top_adapter);

                    category2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            detail_str = category2.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if (pants_str.equals(category1.getSelectedItem().toString())) {
                    //하의
                    ArrayAdapter<String> cloth_pants_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, cloth_pants);
                    cloth_pants_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    category2.setAdapter(cloth_pants_adapter);
                    category2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            detail_str = category2.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if (shoes_str.equals(category1.getSelectedItem().toString())) {
                    //신발
                    ArrayAdapter<String> shoes_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, shoes);
                    shoes_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    category2.setAdapter(shoes_adapter);
                    category2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            detail_str = category2.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if (outer_str.equals(category1.getSelectedItem().toString())) {
                    //아우터
                    ArrayAdapter<String> cloth_outer_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, cloth_outer);
                    cloth_outer_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    category2.setAdapter(cloth_outer_adapter);
                    category2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            detail_str = category2.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if (not_selected_str.equals(category1.getSelectedItem().toString())) {
                    //default
                    ArrayAdapter<String> not_selected_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, not_selected);
                    not_selected_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    category2.setAdapter(not_selected_adapter);
                    category2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            detail_str = category2.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }

                category_str = category1.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //색깔
        ArrayAdapter<String> color_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, colors);
        color_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        color.setAdapter(color_adapter);
        color.setPrompt("색깔");

        color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                color_str = color.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //스타일
        ArrayAdapter<String> style_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, styles);
        style_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        style.setAdapter(style_adapter);
        style.setPrompt("스타일");

        style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                style_str = style.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        Button btn_register = (Button) findViewById(R.id.register_picture);

        //사진 등록 버튼
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                linearLayout.setVisibility(View.VISIBLE);

            }
        });

        Button btn_camera = (Button) findViewById(R.id.btn_camera);

        //사진 촬영 버튼
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.INVISIBLE);
                capture();

            }
        });


        Button btn_album = (Button) findViewById(R.id.btn_album);

        //앨범 선택 버튼
        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.INVISIBLE);
                getPhoto();
            }
        });


        //////////////////////////////////// 취소 버튼
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.INVISIBLE);
            }
        });

        ///////////////////////////////// 옷 등록 버튼
        Button register_cloth = (Button) findViewById(R.id.register_cloth);

        register_cloth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri mTemp = getImageUri(getApplicationContext(), BitmapFactory
                        .decodeFile(photoPath + ".png") );
                name_str = name.getText().toString();
                temp = Uri.fromFile(new File(getPath(temp_getData)));
                StorageReference storageRef = storage.getReference();
                StorageReference riversRef = storageRef.child("images/" + category_str + "/" + detail_str + "/" + color_str + "/" + style_str + "/" + name_str);
                UploadTask uploadTask = riversRef.putFile(mTemp);

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });

                Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_SHORT).show();
                finish();

            }
        });
        // 그랩컷
        tl = new Point();
        br = new Point();
        Button cut_picture = (Button) findViewById(R.id.cut_picture);
        cut_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tl.x = 1;
                tl.y = 1;

                br.x = mImageView.getMaxWidth();
                br.y = mImageView.getMaxHeight();

                Paint rectPaint = new Paint();
                rectPaint.setARGB(255, 255, 0, 0);
                rectPaint.setStyle(Paint.Style.STROKE);
                rectPaint.setStrokeWidth(3);
                Bitmap tmpBm = Bitmap.createBitmap(bitmap.getWidth(),
                        bitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas tmpCanvas = new Canvas(tmpBm);

                tmpCanvas.drawBitmap(bitmap, 0, 0, null);
                tmpCanvas.drawRect(new RectF((float) tl.x, (float) tl.y, (float) br.x, (float) br.y),
                        rectPaint);
                mImageView.setImageDrawable(new BitmapDrawable(getResources(), tmpBm));

                new ProcessImageTask().execute();
            }


        });

    }
    ////////////////////////////////여기 까지가 onCreate

    ///////////////////////////////////on activityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //앨범에서 사진 가져오기
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                mImageView = findViewById(R.id.picture_cloth);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                assert selectedImage != null;
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn,
                        null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                photoPath = cursor.getString(colIndex);
                cursor.close();
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                mImageView.setImageBitmap(bitmap);
                //Uri file = Uri.fromFile(new File(getPath(selectedImage)));


            } catch (IOException e) {
                e.printStackTrace();
            }
            temp_getData = data.getData();
        }

        //카메라로 사진을 찍은 후 사진 가져오기
        else if(requestCode==2){
            try {
                switch (requestCode) {
                    case 2: {
                        if (resultCode == RESULT_OK) {
                            File file = new File(mCurrentPhotoPath);
                            ImageView imageView = (ImageView) findViewById(R.id.picture_cloth);
                            Uri img = Uri.fromFile(file);
                            bitmap = MediaStore.Images.Media
                                    .getBitmap(getContentResolver(), Uri.fromFile(file));
                            if (bitmap != null) {
                                //사진 회전 방지
                                ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                        ExifInterface.ORIENTATION_UNDEFINED);

                                Bitmap rotatedBitmap = null;
                                switch(orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        rotatedBitmap = rotateImage(bitmap, 90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        rotatedBitmap = rotateImage(bitmap, 180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        rotatedBitmap = rotateImage(bitmap, 270);
                                        break;
                                    case ExifInterface.ORIENTATION_NORMAL:
                                    default:
                                        rotatedBitmap = bitmap;
                                }
                                imageView.setImageBitmap(bitmap);
                                temp_getData = getImageUri(getApplicationContext(), bitmap);
                            }
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};

                            assert img != null;
                            Cursor cursor = getContentResolver().query(img, filePathColumn,
                                    null, null, null);
                            assert cursor != null;
                            cursor.moveToFirst();

                            int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                            photoPath = cursor.getString(colIndex);
                            cursor.close();
                        }
                        break;
                    }
                }

            } catch (Exception error) {
                error.printStackTrace();
            }
        }
    }

    //////////////사진 이미지를 파일로 만들기
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    ///////사진 회전하는것 돌려주기
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    ////////사진 경로 가져오기
    public String getPath(Uri uri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
            Log.d("tlqkf",photoPath);
            Mat img = Imgcodecs.imread(photoPath);
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

            Imgcodecs.imwrite(photoPath + ".png", dst);

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            Bitmap jpg = BitmapFactory
                    .decodeFile(photoPath + ".png");

            mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mImageView.setAdjustViewBounds(true);
            mImageView.setPadding(2, 2, 2, 2);
            mImageView.setImageBitmap(jpg);
            mImageView.invalidate();

            dlg.dismiss();
        }
    }

}