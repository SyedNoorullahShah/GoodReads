package com.example.android.goodreads.ui.downloads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.goodreads.database.BookDownloadEntity;
import com.example.android.goodreads.listeners.OnBookItemClickListener;
import com.example.android.goodreads.ui.downloads.DownloadsFragment;
import com.example.android.goodreads.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DownloadsAdapter extends RecyclerView.Adapter {
    List<BookDownloadEntity> downloads;
    OnBookItemClickListener bookItemClickListener;
    LayoutInflater inflater;

    public DownloadsAdapter(DownloadsFragment downloadsFragment, ArrayList<BookDownloadEntity> value) {
        downloads = value;
        bookItemClickListener = downloadsFragment;
        inflater = downloadsFragment.getLayoutInflater();
    }

    public void updateDownloads(List<BookDownloadEntity> newData){
        downloads.clear();
        downloads.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.book_item, parent, false);
        return new BookHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BookHolder bookHolder = (BookHolder) holder;
        BookDownloadEntity book = downloads.get(position);

        bookHolder.setBookId(book.getBookId());
        bookHolder.setBookTitle(book.getTitle());
        bookHolder.setBookAuthor(book.getAuthor());
        bookHolder.setBookCover(book.getImage());
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public BookDownloadEntity getDownload(int adapterPosition) {
        return downloads.get(adapterPosition);
    }


    private class BookHolder extends RecyclerView.ViewHolder {
        private TextView bookTitle, bookAuthor;
        private ImageView bookCover;
        private String bookId;

        public BookHolder(@NonNull final View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.title_detail);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookCover = itemView.findViewById(R.id.cover_detail);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open book detail activity...
                    bookItemClickListener.onBookItemClicked(bookId, bookTitle.getText().toString());
                }
            });
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle.setText(bookTitle);
        }

        public void setBookAuthor(String bookAuthor) {
            this.bookAuthor.setText(bookAuthor);
        }

        public void setBookCover(String bookCover) {
            Picasso.get().load(bookCover).resize(128, 0).placeholder(R.drawable.book_placeholder).into(this.bookCover);
        }


        public void setBookId(String id) {
            bookId = id;
        }
    }
}
