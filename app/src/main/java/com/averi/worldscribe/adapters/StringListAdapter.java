package com.averi.worldscribe.adapters;

import android.support.v7.widget.RecyclerView;
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
        this.strings = strings;
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

}
