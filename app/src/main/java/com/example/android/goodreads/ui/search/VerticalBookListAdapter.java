package com.example.android.goodreads.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.ui.search.SearchActivity;
import com.example.android.justjava.R;
import com.example.android.goodreads.listeners.OnBookItemClickListener;
import com.example.android.goodreads.listeners.UpdateListListener;
import com.example.android.goodreads.network.BookNetworkAccess;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class VerticalBookListAdapter extends RecyclerView.Adapter {
    private final LayoutInflater layoutInflater;
    private final ArrayList<BookListItem> bookItemData;
    private OnBookItemClickListener bookItemClickListener;
    private UpdateListListener updateListListener;
    private static final int VIEW_TYPE_NULL = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    public static boolean isDataHandled;

    public VerticalBookListAdapter(SearchActivity currentActivity, ArrayList<BookListItem> books) {
        layoutInflater = currentActivity.getLayoutInflater();
        bookItemData = books;
        bookItemClickListener = currentActivity;
        updateListListener = currentActivity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View v = layoutInflater.inflate(R.layout.book_item, viewGroup, false);
            return new BookHolder(v);
        } else {
            View v = layoutInflater.inflate(R.layout.loading_item, viewGroup, false);
            return new LoadingHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return bookItemData.get(position) == null ? VIEW_TYPE_NULL : VIEW_TYPE_ITEM;
    }

    public ArrayList<BookListItem> getBookItemData() {
        return bookItemData;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        if (viewHolder instanceof BookHolder) {
            BookHolder bookHolder = (BookHolder) viewHolder;

            bookHolder.setBookTitle(bookItemData.get(i).getTitle());
            bookHolder.setBookAuthor(bookItemData.get(i).getAuthor());
            bookHolder.setBookCover(bookItemData.get(i).getImage());
            bookHolder.setBookId(bookItemData.get(i).getBookId());

        } else {
            LoadingHolder loadingHolder = (LoadingHolder) viewHolder;
            loadingHolder.showLoadingBar(BookNetworkAccess.isInFetchListProcess());
        }
    }

    public void updateBooks(ArrayList<BookListItem> newData, boolean updateSameList) {
        if (isDataHandled) return;

        if (!updateSameList && !bookItemData.contains(newData.get(0))) { //don't update same list
            bookItemData.clear();
            bookItemData.addAll(newData);
            notifyDataSetChanged();

        } else if (updateSameList) {
            bookItemData.remove(bookItemData.size() - 1);

            if (newData.size() > 1) {
                bookItemData.addAll(bookItemData.size() - 1, newData);
                notifyItemInserted(bookItemData.size() - 1);
            } else {
                notifyItemRemoved(bookItemData.size() - 1);
            }
        }
        isDataHandled = true;
    }

    @Override
    public int getItemCount() {
        return bookItemData.size();
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {
        private ProgressBar loadingBar;
        private ImageButton retryBtn;

        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
            loadingBar = itemView.findViewById(R.id.load_results);
            retryBtn = itemView.findViewById(R.id.btn_retry);
            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookItemData.remove(bookItemData.size() - 1);
                    notifyItemRemoved(bookItemData.size());
                    updateListListener.updateList(bookItemData);
                }
            });
        }

        public void showLoadingBar(boolean stillLoading) {
            int loadingVisibility = stillLoading ? View.VISIBLE : View.GONE;
            int retryVisibility = !stillLoading ? View.VISIBLE : View.GONE;
            loadingBar.setVisibility(loadingVisibility);
            retryBtn.setVisibility(retryVisibility);
        }
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
