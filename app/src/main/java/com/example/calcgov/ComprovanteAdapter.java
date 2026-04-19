package com.example.calcgov;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComprovanteAdapter extends RecyclerView.Adapter<ComprovanteAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onDeleteClick(File file, int position);
    }

    private List<File> files;
    private OnItemClickListener clickListener;

    public ComprovanteAdapter(List<File> files, OnItemClickListener clickListener) {
        this.files = files;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comprovante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = files.get(position);
        holder.txtNome.setText(file.getName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.txtData.setText(sdf.format(new Date(file.lastModified())));

        Glide.with(holder.itemView.getContext())
                .load(file)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.img);

        holder.btnDelete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeleteClick(file, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void removeAt(int position) {
        files.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, files.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtNome, txtData;
        View btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgComprovante);
            txtNome = itemView.findViewById(R.id.txtNomeArquivo);
            txtData = itemView.findViewById(R.id.txtDataArquivo);
            btnDelete = itemView.findViewById(R.id.btnDeleteComprovante);
        }
    }
}
