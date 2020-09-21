package com.example.android.goodreads.ui.bookdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.R;
import com.example.android.goodreads.databinding.RelatedBookItemBinding;
import com.example.android.goodreads.listeners.OnBookItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalBookListAdapter extends RecyclerView.Adapter {
    private ArrayList<BookListItem> relatedBooks;
    private LayoutInflater inflater;
    private OnBookItemClickListener onBookItemClickListener;

    public HorizontalBookListAdapter(FragmentActivity currentActivity) {
        relatedBooks = new ArrayList<>();
        inflater = currentActivity.getLayoutInflater();
        onBookItemClickListener = (OnBookItemClickListener) currentActivity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.related_book_item, parent, false);
        return new RelatedBookHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RelatedBookHolder relatedBookHolder = (RelatedBookHolder) holder;
        relatedBookHolder.setBookCover(relatedBooks.get(position).getImage());
        relatedBookHolder.setBookTitle(relatedBooks.get(position).getTitle());
        relatedBookHolder.setBookAuthor(relatedBooks.get(position).getAuthor());
        relatedBookHolder.setBookId(relatedBooks.get(position).getBookId());
    }

    @Override
    public int getItemCount() {
        return (Math.min(relatedBooks.size(), 15));
    }

    public void updateList(ArrayList<BookListItem> bookItems) {
        relatedBooks.clear();
        if(bookItems != null) relatedBooks.addAll(bookItems);
        notifyDataSetChanged();
    }

    private class RelatedBookHolder extends RecyclerView.ViewHolder {
        RelatedBookItemBinding relatedBookItem;
        private String bookId;

        public RelatedBookHolder(@NonNull final View itemView) {
            super(itemView);
            relatedBookItem = RelatedBookItemBinding.bind(itemView);
            relatedBookItem.bookItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBookItemClickListener.onBookItemClicked(bookId, relatedBookItem.titleDetail.getText().toString());
                }
            });
        }


        public void setBookTitle(String bookTitle) {
            relatedBookItem.titleDetail.setText(bookTitle);
        }

        public void setBookAuthor(String bookAuthor) {
            relatedBookItem.bookAuthor.setText(bookAuthor);
        }

        public void setBookCover(String bookCover) {
            Picasso.get().load(bookCover).resize(128, 0).placeholder(R.drawable.book_placeholder).into(relatedBookItem.coverDetail);
        }

        public void setBookId(String id) {
            bookId = id;
        }

    }
}
