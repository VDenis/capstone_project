package com.denis.home.sunnynotes.noteList;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.data.NoteProvider;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    //private int mPosition = RecyclerView.NO_POSITION;
    private NoteListAdapter mNoteListAdapter;

    private static final int CURSOR_LOADER_ID = 0;

    public NoteListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_note_list, container, false);

        mNoteListAdapter = new NoteListAdapter(getActivity(), new NoteListAdapter.NoteListAdapterOnClickHandler() {
            @Override
            public void onClick(int noteId, NoteListAdapter.NoteListAdapterViewHolder vh) {
                int position = vh.getAdapterPosition();
                String text = "Click on note, id: " + noteId + ", position: " + position;
                Toast.makeText(getActivity(),
                        text,
                        Toast.LENGTH_SHORT).show();
                Timber.d(text);
            }
        });

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_notes);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setAdapter(mNoteListAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since update database, when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        //String sortOrder = NoteColumns.SERVER_MOD_TIME + " ASC";
        String sortOrder = null;

        Uri notesUri = NoteProvider.Notes.CONTENT_URI;
        return new CursorLoader(getActivity(),
                notesUri,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mNoteListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNoteListAdapter.swapCursor(null);
    }
}
