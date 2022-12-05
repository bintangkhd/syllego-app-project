package com.example.nye_ken;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    String folderPath = Environment.getDataDirectory().getAbsolutePath() + "/storage/emulated/0/hasil_pdf";
    File directory = new File("/storage/emulated/0/hasil_pdf/");

    Button convert;
    ImageView openGallery, img;
    TextView title;
    ProgressBar progressBar;

    private ArrayList <String> path = new ArrayList<>();
    private ArrayList <String> name = new ArrayList<>();

    ArrayList<Model> pdfList = new ArrayList<>();
    RecyclerView recyclerView;
    FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.converting);
        
        askPermission();

        openGallery = (ImageView) findViewById(R.id.converting_gallery);
        convert = (Button) findViewById(R.id.converting_convert);
        img = (ImageView) findViewById(R.id.converting_imageView);
        title = (TextView) findViewById(R.id.converting_fileName);
        progressBar = (ProgressBar) findViewById(R.id.converting_progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.pdf_recyclerView);


        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (directory.exists()) {
                    progressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HomeActivity.this, "try again",
                            Toast.LENGTH_LONG).show();
                    createFolder();
                }
            }
        });

        loadPDFFiles();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.GONE);
            if (data != null) {
                ClipData clipData = data.getClipData();
                ArrayList<Uri> selectedImage = new ArrayList<>();

                for (int i = 0; i < clipData.getItemCount(); i++) {
                    selectedImage.add(clipData.getItemAt(i).getUri());

                    String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
                    Cursor cursor = getContentResolver().query(selectedImage.get(i), filePathColumn, null, null);
                    cursor.moveToFirst();
                    int filePath = cursor.getColumnIndex(filePathColumn[0]);
                    int fileName = cursor.getColumnIndex(filePathColumn[1]);
                    path.add(cursor.getString(filePath));
                    name.add(cursor.getString(fileName));
                    cursor.close();

                    img.setImageURI(Uri.parse(path.get(i)));
                    title.setText(name.get(i));

                }

                convert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressBar.setVisibility(View.VISIBLE);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean converted = imageToPDF(path, name);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        if (!converted) {
                                            Toast.makeText(HomeActivity.this, "fail",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }
                        });
                        thread.start();
                    }
                });

            } else {
                Toast.makeText(this, "Select a image", Toast.LENGTH_SHORT).show();
            }
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(
                        HomeActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomeActivity.this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
            },REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE) {
            createFolder();
        }
    }

    private void createFolder() {
        File folder = new File(folderPath);
        if(!folder.exists()) {
            folder.mkdir();
        }
        directory.mkdirs();
    }

    private boolean imageToPDF(ArrayList<String> path, ArrayList<String> name) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<Bitmap> bitmap = new ArrayList<>();
            PdfDocument pdfDocument = new PdfDocument();

            for (int i = 0; i < path.size(); i++) {

                bitmap.add(BitmapFactory.decodeFile(path.get(i)));

                int height = bitmap.get(i).getHeight();
                int width = bitmap.get(i).getWidth();

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 0).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                page.getCanvas().drawBitmap(bitmap.get(i), 0, 0, null);
                pdfDocument.finishPage(page);
            }
                String changeName = getWithoutExtension(name.get(0));
                String newPath = directory + "/" + changeName + ".pdf";
                File file = new File(newPath);
                try {
                    pdfDocument.writeTo(new FileOutputStream(file));
                    MediaScannerConnection.scanFile(HomeActivity.this, new String[]{
                            file.toString()
                    }, null, null);
                } catch (Exception e) {
                    e.getStackTrace();
                }
                pdfDocument.close();
                return true;

        } else {
            return false;
        }
    }

    private String getWithoutExtension(String name) {

        return "Hasil PDF dari " + name.substring(0, name.lastIndexOf("."));
    }

    private void loadPDFFiles() {
        pdfList = loadFiles(this);
        fileAdapter = new FileAdapter(this, pdfList);
        recyclerView.setAdapter(fileAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
    }

    private String convertIntoReadableSize(long size) {
        String s = "";
        long kilo = 1024;
        long mega = kilo * kilo;
        long giga = mega * kilo;
        long tera = giga * kilo;

        double kb = (double) size / kilo;
        double mb = kb / kilo;
        double gb = mb / kilo;
        double tb = gb / kilo;

        if (size < kilo) {
            s = size + " Bytes";
        } else if (size >= kilo && size < mega) {
            s = String.format("%.2f", kb) + " KB";
        } else if (size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB";
        } else if (size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB";
        } else if (size >= tera) {
            s = String.format("%.2f", tb) + " TB";
        }

        return s;
    }

    public ArrayList<Model> loadFiles(Context context) {
        ArrayList<Model> arrayList = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
        };

        String mimeType = "application/pdf";
        String whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeType + "')";
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        Cursor cursor = context.getContentResolver().query(uri, projection, whereClause, null, sortOrder);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int idPath = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int idTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
        int idSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(idColumn);
                String path = cursor.getString(idPath);
                String title = cursor.getString(idTitle);
                long size = cursor.getLong(idSize);

                Model model = new Model(id, path, title, convertIntoReadableSize(size));
                arrayList.add(model);
            }
        }

        return arrayList;
    }

}



