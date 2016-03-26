package com.denis.home.sunnynotes.noteList;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.denis.home.sunnynotes.R;
import com.denis.home.sunnynotes.Utility;
import com.denis.home.sunnynotes.data.NoteColumns;

/**
 * Created by Denis on 20.03.2016.
 */
public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteListAdapterViewHolder> {

    private static final int VIEW_TYPE_STANDARD = 0;
    //private static final int VIEW_TYPE_FAVORITE = 1;
    final private Context mContext;
    final private NoteListAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    public NoteListAdapter(Context context, NoteListAdapterOnClickHandler dh) {
        mContext = context;
        mClickHandler = dh;
    }

    @Override
    public NoteListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_STANDARD: {
                    layoutId = R.layout.list_item_note;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new NoteListAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(NoteListAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String noteName = mCursor.getString(mCursor.getColumnIndex(NoteColumns.FILE_NAME));
        noteName = Utility.stripDotTxtInString(noteName);
        holder.mNoteNameView.setText(noteName);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public static interface NoteListAdapterOnClickHandler {
        void onClick(int noteId, NoteListAdapterViewHolder vh);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class NoteListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mNoteNameView;

        public NoteListAdapterViewHolder(View view) {
            super(view);
            mNoteNameView = (TextView) view.findViewById(R.id.list_item_note_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int noteIdColumnIndex = mCursor.getColumnIndex(NoteColumns._ID);
            mClickHandler.onClick(mCursor.getInt(noteIdColumnIndex), this);
        }
    }
}
