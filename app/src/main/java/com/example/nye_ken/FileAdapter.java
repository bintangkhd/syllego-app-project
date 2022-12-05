package com.example.nye_ken;

import android.content.Context;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyHolder> {

    Context context;
    static ArrayList<Model> pdfAdapterArray = new ArrayList<>();

    public FileAdapter(Context context, ArrayList<Model> pdfAdapterArray) {
        this.context = context;
        FileAdapter.pdfAdapterArray = pdfAdapterArray;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_view, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.MyHolder holder, int a) {
        holder.title.setText(pdfAdapterArray.get(a).getTitle());
        holder.size.setText(pdfAdapterArray.get(a).getSize());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PDFViewActivity.class);
                intent.putExtra("position", a);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfAdapterArray.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        TextView title, size;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.file_view_title);
            size = itemView.findViewById(R.id.file_view_size);
        }
    }
}
