package com.denis.home.sunnynotes.noteList;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.denis.home.sunnynotes.BuildConfig;
import com.denis.home.sunnynotes.MainActivity;
import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteProvider;
import com.denis.home.sunnynotes.dropbox.DropboxFragment;
import com.denis.home.sunnynotes.service.SunnyNotesServiceHelper;
import com.dropbox.core.android.Auth;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends DropboxFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    //private int mPosition = RecyclerView.NO_POSITION;
    private NoteListAdapter mNoteListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    android.support.v7.app.ActionBar mActionBar;
    SwipeRefreshLayout.OnRefreshListener mSwipeRefreshListner;

    private static final int CURSOR_LOADER_ID = 0;
    private boolean mFirstLaunch = false;

    public NoteListFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof MainActivity) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.main, menu);
            //finishCreatingMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            //TODO: crash
/*            if (checkAuthState()) {
                SunnyNotesServiceHelper.Sync(getActivity());
                return true;
            }*/
            runUpdate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void loadData() {
        if (mFirstLaunch) {
            runUpdate();
        }
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean newValue = intent.getBooleanExtra(SunnyNotesServiceHelper.Constants.EXTENDED_DATA_STATUS, false);
            Timber.d("IsRefreshing change value to: " + newValue);
            updateRefreshingUI(false);

        }
    };

    private void updateRefreshingUI(boolean value) {
        mSwipeRefreshLayout.setRefreshing(value);
    }

    @Override
    protected void registrationInterruption() {
        //Intent intent = new Intent(getActivity(), MainActivity.class);
        //startActivity(intent);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri noteIdUri);
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
                // ToDO: id or file id
                ((Callback) getActivity())
                        .onItemSelected(NoteProvider.Notes.withId(noteId));
                //TODO: delete debug toast
                String text = "Click on note, id: " + noteId + ", position: " + position;
/*                Toast.makeText(getActivity(),
                        text,
                        Toast.LENGTH_SHORT).show();*/
                Timber.d(text);
            }
        });

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_notes);
        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mNoteListAdapter);


        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.app_name);
        }

        // Callback
        IntentFilter mStatusIntentFilter = new IntentFilter(
                SunnyNotesServiceHelper.Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mRefreshingReceiver,
                mStatusIntentFilter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                runUpdate();
            }
        };
/*        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                runUpdate();
            }
        });*/
        mSwipeRefreshLayout.setOnRefreshListener(mSwipeRefreshListner);


        // Ads
        AdView adView = (AdView) rootView.findViewById(R.id.AdView);
        AdRequest adRequest = new AdRequest.Builder().
                addTestDevice(AdRequest.DEVICE_ID_EMULATOR).
                addTestDevice(getActivity().getString(R.string.ad_mob_test_device)).
                build();
        adView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //checkAuthState();

        // It's seems to be a bug in SwipeRefreshLayout
        //runUpdate();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                // directly call onRefresh() method
                mSwipeRefreshListner.onRefresh();
            }
        });

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void runUpdate() {
        if (hasToken()) {
            // Check internet
            if (Utility.isNetworkAvailable(getActivity())) {
                updateRefreshingUI(true);
                SunnyNotesServiceHelper.Sync(getActivity());
                Timber.d("Run SunnyNotesServiceHelper.Sync after user login");
            } else {
                updateRefreshingUI(false);
                Toast.makeText(getActivity(),
                        getString(R.string.empty_note_list_no_network),
                        Toast.LENGTH_SHORT).
                        show();
            }
        } else {
            Auth.startOAuth2Authentication(getActivity(), BuildConfig.DROPBOX_APP_KEY_JAVA);
            mFirstLaunch = true;
        }
    }

/*    private boolean checkAuthState() {
        if (!hasToken()) {
            Auth.startOAuth2Authentication(getActivity(), BuildConfig.DROPBOX_APP_KEY_JAVA);
            return false;
        }
        return true;
    }*/

    // since update database, when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        //String sortOrder = NoteColumns.SERVER_MOD_TIME + " ASC";
        //String sortOrder = NoteColumns.SERVER_MOD_TIME + " DESC";
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
        //mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNoteListAdapter.swapCursor(null);
    }
}
