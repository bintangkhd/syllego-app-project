package com.example.nye_ken;

import static com.example.nye_ken.FileAdapter.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PDFViewActivity extends AppCompatActivity {

    int position = -1;
    PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        position = getIntent().getIntExtra("position", 0);
        pdfView = findViewById(R.id.pdfView);
        displayPDF(position);
    }

    private void displayPDF(int position) {
        String path = pdfAdapterArray.get(position).getPath();
        File file = new File(path);
        pdfView.fromFile(file)
                .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .load();
    }

}