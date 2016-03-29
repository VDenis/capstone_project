package com.denis.home.sunnynotes.noteDetail;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.denis.home.sunnynotes.BuildConfig;
import com.denis.home.sunnynotes.CustomApplication;
import com.denis.home.sunnynotes.MainActivity;
import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteColumns;
import com.denis.home.sunnynotes.dropbox.DropboxFragment;
import com.denis.home.sunnynotes.service.ActionServiceHelper;
import com.dropbox.core.android.Auth;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoteDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteDetailFragment extends DropboxFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;
    private static final String SUNNY_NOTES_SHARE_HASHTAG = " #SunnyNotes";
    private Uri mUri;
    private String mShareNote;


    private EditText mNoteTitleView;
    private EditText mNoteContentView;
    private boolean isAddMode = false;
    private android.support.v7.app.ActionBar mActionBar;
    private Tracker mTracker;

    public NoteDetailFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param noteUri content uri .
     * @return A new instance of fragment NoteDetailFragment.
     */
    public static NoteDetailFragment newInstance(Uri noteUri) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_URI, noteUri);
        fragment.setArguments(args);
        return fragment;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share_note);
        menuItem.setIntent(createShareNoteIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof NoteDetailActivity || getActivity() instanceof MainActivity) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detail, menu);
            //finishCreatingMenu(menu);
        }
    }

    private Intent createShareNoteIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareNote + SUNNY_NOTES_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_note) {
            if (mUri != null) {
                if (hasToken()) {
                    // Check internet
                    if (Utility.isNetworkAvailable(getActivity())) {
                        Utility.deleteTxtFile(getActivity(), mUri);
                        ActionServiceHelper.Delete(getActivity(), mUri);

                        boolean useDetailActivity = getActivity().getResources()
                                .getBoolean(R.bool.use_detail_activity);
                        if (useDetailActivity) {
                            getActivity().finish();
                        } else {
                            mNoteTitleView.setText("");
                            mNoteContentView.setText("");
                        }
                    } else {
                        Toast.makeText(getActivity(),
                                getString(R.string.note_detail_no_network),
                                Toast.LENGTH_SHORT).
                                show();
                    }
                } else {
                    Auth.startOAuth2Authentication(getActivity(), BuildConfig.DROPBOX_APP_KEY_JAVA);
                }
            } else {
                Toast.makeText(getActivity(),
                        getString(R.string.note_detail_note_dont_created),
                        Toast.LENGTH_SHORT).
                        show();
            }
            return true;
        } else if (id == R.id.action_save_note) {
            if (mUri != null) {
                String filename = mNoteTitleView.getText().toString();
                String content = mNoteContentView.getText().toString();

                if (hasToken()) {
                    if (Utility.isValidNoteTitle(filename)) {
                        if (!Utility.getNoteIdIfExist(getActivity(), filename, mUri)) {
                            // Check internet
                            if (Utility.isNetworkAvailable(getActivity())) {
                                Utility.updateTxtFile(getActivity(), mUri, content);
                                ActionServiceHelper.Update(getActivity(), mUri, filename);
                            } else {
                                Toast.makeText(getActivity(),
                                        getString(R.string.note_detail_no_network),
                                        Toast.LENGTH_SHORT).
                                        show();
                            }
                        } else {
                            mNoteTitleView.setError(getString(R.string.error_note_exist));
                        }
                    } else {
                        mNoteTitleView.setError(getString(R.string.error_invalid_note_title));
                    }
                } else {
                    Auth.startOAuth2Authentication(getActivity(), BuildConfig.DROPBOX_APP_KEY_JAVA);
                }
            } else {
                String filename = mNoteTitleView.getText().toString();
                String content = mNoteContentView.getText().toString();
                if (hasToken()) {
                    if (Utility.isValidNoteTitle(filename)) {
                        if (!Utility.getNoteIdIfExist(getActivity(), filename)) {
                            // Check internet
                            if (Utility.isNetworkAvailable(getActivity())) {
                                filename = mNoteTitleView.getText().toString();
                                content = mNoteContentView.getText().toString();
                                String filePath = Utility.createTxtFile(getActivity(), filename, content);
                                ActionServiceHelper.Add(getActivity(), filePath);
                            } else {
                                Toast.makeText(getActivity(),
                                        getString(R.string.note_detail_no_network),
                                        Toast.LENGTH_SHORT).
                                        show();
                            }
                        } else {
                            mNoteTitleView.setError(getString(R.string.error_note_exist));
                        }
                    } else {
                        mNoteTitleView.setError(getString(R.string.error_invalid_note_title));
                    }
                } else {
                    Auth.startOAuth2Authentication(getActivity(), BuildConfig.DROPBOX_APP_KEY_JAVA);
                }
            }
            return true;
        } else if (id == R.id.action_share_note) {
            String noteName = mNoteTitleView.getText().toString();
            String noteContent = mNoteContentView.getText().toString();
            mShareNote = String.format("%s:\n%s", noteName, noteContent);
            Intent intent = createShareNoteIntent();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUri = getArguments().getParcelable(DETAIL_URI);
            if (mUri == null) {
                isAddMode = true;
            }
        }
    }

    @Override
    public void onResume() {
        mTracker.setScreenName(getString(R.string.analytics_note_detail_page));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        super.onResume();
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected void registrationInterruption() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_note_detail, container, false);

        mNoteTitleView = (EditText) rootView.findViewById(R.id.note_title_edittext_view);
        mNoteContentView = (EditText) rootView.findViewById(R.id.note_content_edittext_view);

        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (!isAddMode) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }

        CustomApplication application = (CustomApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String noteName = data.getString(data.getColumnIndex(NoteColumns.FILE_NAME));
            noteName = Utility.stripDotTxtInString(noteName);

            String lowerPath = data.getString(data.getColumnIndex(NoteColumns.LOWER_PATH));

            String fileContent = Utility.readTxtFile(getActivity(), lowerPath);

            Timber.d("Loader finesh, show note with title: " + noteName);
            mNoteTitleView.setText(noteName);
            mNoteContentView.setText(fileContent);

            //mShareNote = String.format("%s:\n%s", noteName, fileContent);

            boolean useDetailActivity = getActivity().getResources()
                    .getBoolean(R.bool.use_detail_activity);
            if (mActionBar != null && useDetailActivity) {
                mActionBar.setTitle(noteName);
            }

/*            AppCompatActivity activity = (AppCompatActivity)getActivity();
            Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.main_toolbar);
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detail);
                finishCreatingMenu(toolbarView.getMenu());
            }*/
            //mNoteTitleView.setText("Hi HI hI debug");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    new String[]{NoteColumns.FILE_ID, NoteColumns.FILE_NAME, NoteColumns.DISPLAY_PATH, NoteColumns.LOWER_PATH},
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
