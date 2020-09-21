package com.example.android.goodreads.ui.bookshelves;

import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.goodreads.ui.bookshelves.BookshelvesFragment;
import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.data.BookShelf;
import com.example.android.goodreads.ui.bookdetail.HorizontalBookListAdapter;
import com.example.android.justjava.R;
import com.example.android.justjava.databinding.BookshelfItemBinding;
import com.example.android.goodreads.listeners.OnViewAllChildListListener;

import java.util.ArrayList;

class BookshelfAdapter extends RecyclerView.Adapter {
    private ArrayList<BookShelf> bookShelves;
    private FragmentActivity ctx;
    private OnViewAllChildListListener viewAllListener;

    public BookshelfAdapter(BookshelvesFragment bookshelvesFragment) {
        bookShelves = new ArrayList<>();
        ctx = bookshelvesFragment.getActivity();
        viewAllListener = bookshelvesFragment;
    }

    public void updateList(ArrayList<BookShelf> bookShelves) {
        this.bookShelves.clear();
        this.bookShelves.addAll(bookShelves);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.bookshelf_item, parent, false);
        return new BookShelfHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BookShelfHolder bookShelfHolder = (BookShelfHolder) holder;
        BookShelf bookShelf = bookShelves.get(position);

        bookShelfHolder.setBookShelfId(bookShelf.getId());
        bookShelfHolder.setBookShelfName(bookShelf.getTitle());
        bookShelfHolder.setBookShelfChildList(bookShelf.getBookItems());
        bookShelfHolder.setBtnViewAll((bookShelf.getBookItems()) != null && (bookShelf.getBookItems().size() > 15));
        bookShelfHolder.setBtnDialogInfo(bookShelf.getTitle().equals("Recently viewed"));
    }

    @Override
    public int getItemCount() {
        return bookShelves.size();
    }

    public void detachContext() {
        ctx = null;
    }

    private class BookShelfHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private String id;
        private TextView bookShelfName, empty_msg;
        private Button btn_viewAll;
        private ImageButton btn_dialog;
        private RecyclerView bookshelvesChildList;

        public BookShelfHolder(View v) {
            super(v);
            BookshelfItemBinding bookshelfItemBinding = BookshelfItemBinding.bind(v);

            bookShelfName = bookshelfItemBinding.bookshelfName;
            empty_msg = bookshelfItemBinding.emptyMsg;
            btn_viewAll = bookshelfItemBinding.btnViewAll;
            btn_dialog = bookshelfItemBinding.btnDialogInfo;
            btn_dialog.setOnClickListener(this);
            btn_viewAll.setOnClickListener(this);
            bookshelvesChildList = bookshelfItemBinding.bookshelvesChildList;
            bookshelvesChildList.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
            bookshelvesChildList.setAdapter(new HorizontalBookListAdapter(ctx));
        }

        public void setBookShelfId(String shelfId) {
            id = shelfId;
        }

        public void setBookShelfName(String title) {
            bookShelfName.setText(title);
        }

        public void setBookShelfChildList(ArrayList<BookListItem> bookItems) {
            Log.d("azhar", "setBookShelfChildList: ");
            HorizontalBookListAdapter adapter = (HorizontalBookListAdapter) bookshelvesChildList.getAdapter();

            if (bookItems == null || bookItems.size() == 0) {        //either there was some error while fetching bookshelf's list items or the bookshelf is empty
                empty_msg.setVisibility(View.VISIBLE);
                String errorMsg = bookItems == null ? "Couldn't get this bookshelf." : "No books in this bookshelf yet.";
                adapter.updateList(bookItems);
                empty_msg.setText(errorMsg);
            } else {                                //bookshelf items exist.. update this adapter's list
                empty_msg.setVisibility(View.GONE);
                adapter.updateList(bookItems);
            }
        }

        public void setBtnViewAll(boolean isEnabled) {
            btn_viewAll.setTextColor(isEnabled ? ContextCompat.getColor(ctx, R.color.colorPrimary) : Color.GRAY);
            btn_viewAll.setEnabled(isEnabled);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btn_view_all)
                viewAllListener.onViewAllClicked(id, (String) bookShelfName.getText());
            else
                showDialog();
        }

        private void showDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Make sure to use your browser's google account.");
            builder.setMessage("For updating 'Recently Viewed', your google account used in Good Reads and in your browser should be same.");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }

        public void setBtnDialogInfo(boolean isVisible) {
            int visibility = isVisible ? View.VISIBLE : View.GONE;
            btn_dialog.setVisibility(visibility);
        }
    }
}
