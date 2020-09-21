package com.example.android.goodreads.ui.downloads;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.goodreads.database.BookDownloadEntity;
import com.example.android.goodreads.ui.bookdetail.BookDetailActivity;
import com.example.android.goodreads.databinding.FragmentDownloadsBinding;
import com.example.android.goodreads.listeners.OnBookItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends Fragment implements OnBookItemClickListener {

    public static final String TAG = "NOOR";
    private DownloadModel downloadModel;
    private FragmentDownloadsBinding root;
    private LiveData<List<BookDownloadEntity>> downloads;
    private DownloadsAdapter downloadsAdapter;
    private Application application;

    private Observer<List<BookDownloadEntity>> observer = new Observer<List<BookDownloadEntity>>() {
        @Override
        public void onChanged(List<BookDownloadEntity> bookDownloadEntities) {
            if (bookDownloadEntities.isEmpty()) {
                toggleViews(View.GONE, View.VISIBLE);
            } else {
                toggleViews(View.VISIBLE, View.GONE);
                downloadsAdapter.updateDownloads(bookDownloadEntities);
            }
        }
    };

    private void toggleViews(int listVis, int msgVis) {
        root.downloadList.setVisibility(listVis);
        root.tempErrMsg.setVisibility(msgVis);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        application = (Application) context.getApplicationContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "DOWNLOADS FRAGMENT -----> onCreate: ");
        downloadModel = new ViewModelProvider.AndroidViewModelFactory(application).create(DownloadModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = FragmentDownloadsBinding.inflate(inflater, container, false);
        return root.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "DOWNLOADS FRAGMENT -----> onViewCreated: ");
        downloads = downloadModel.getDownloads();
        downloads.observe(getViewLifecycleOwner(), observer);
        setRecyclerView();
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = root.downloadList;
        downloadsAdapter = new DownloadsAdapter(this, new ArrayList<BookDownloadEntity>());
        recyclerView.setAdapter(downloadsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                BookDownloadEntity bookDownloadEntity = downloadsAdapter.getDownload(viewHolder.getAdapterPosition());
                downloadModel.deleteDownload(bookDownloadEntity);
                File file = new File(application.getFilesDir().getAbsolutePath(), bookDownloadEntity.getBookId() + ".pdf");
                file.delete();
                downloadsAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onBookItemClicked(String bookId, String bookTitle) {
        Intent intent = new Intent(getContext(), BookDetailActivity.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
    }

}
