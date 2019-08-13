package com.averi.worldscribe.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.averi.worldscribe.R;

import java.util.ArrayList;

/**
 * Created by mark on 15/06/16.
 */
public class StringListAdapter extends RecyclerView.Adapter<StringListAdapter.ViewHolder> {

    private ArrayList<String> strings;
    private ArrayList<String> stringsCopy;    // For adding back items during filtering
    private StringListContext context;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        private StringListContext context;

        public ViewHolder(StringListContext context, TextView textView) {
            super(textView);
            this.textView = textView;
            this.context = context;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            context.respondToListItemSelection(textView.getText().toString());
        }
    }

    public StringListAdapter(StringListContext context, ArrayList<String> strings) {
        this.strings = new ArrayList<>(strings);
        this.stringsCopy = new ArrayList<>(strings);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_text, parent,
                false);
        return new ViewHolder(context, (TextView) view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(strings.get(position));
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    /**
     * Set a new list of strings as this Adapter's content.
     * @param strings The new contents held by this Adapter.
     */
    public void updateList(ArrayList<String> strings) {
        this.strings.clear();
        stringsCopy.clear();
        this.strings.addAll(strings);
        stringsCopy.addAll(strings);
    }

    /**
     * Filters the items in this Adapter to include only items that contain the
     * specified query string.
     * @param query A string of text meant to match one or more items in this Adapter
     */
    public void filterQuery(String query) {
        strings.clear();

        if (query.isEmpty()) {    // Empty query means user hasn't entered anything yet.
            Log.d("WorldScribe", String.valueOf(stringsCopy.size()));
            strings.addAll(stringsCopy);
        } else {
            query = query.toLowerCase();   // Searches are case-insensitive.
            for (String string : stringsCopy) {
                if (string.toLowerCase().contains(query)) {
                    strings.add(string);
                }
            }
        }

        notifyDataSetChanged();
    }

}
