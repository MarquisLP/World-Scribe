package com.averi.worldscribe.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.SnippetActivity;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying an Article's Snippets.
 */
public class SnippetsAdapter extends RecyclerView.Adapter<SnippetsAdapter.SnippetHolder> {

    /**
     * A ViewHolder containing a Snippet Card. Clicking on it will navigate the user to
     * SnippetActivity where they can edit the selected Snippet. The card also contains an Erase
     * Button for deleting the selected Snippet.
     */
    public class SnippetHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView snippetCard;
        private final TextView snippetNameText;
        private final ImageButton deleteButton;
        private final String worldName;
        private final Category category;
        private final String articleName;
        private String snippetName;

        /**
         * Instantiates a new SnippetHolder.
         * @param context The Context calling this method.
         * @param worldName The name of the current World.
         * @param category The {@link Category} of the current Article.
         * @param articleName The name of the current Article.
         * @param itemView The Snippet Card that this ViewHolder will handle.
         */
        public SnippetHolder(Context context, String worldName, Category category,
                             String articleName, View itemView) {
            super(itemView);

            this.context = context;
            snippetCard = (CardView) itemView;
            snippetNameText = (TextView) snippetCard.findViewById(R.id.snippetName);
            deleteButton = (ImageButton) snippetCard.findViewById(R.id.delete);
            this.worldName = worldName;
            this.category = category;
            this.articleName = articleName;

            snippetCard.setOnClickListener(this);
        }

        /**
         * Stores the name of the Snippet and display it.
         * @param snippetName The name of the Snippet represented by the card.
         */
        public void bindSnippet(String snippetName) {
            this.snippetName = snippetName;
            setSnippetName();
        }

        /**
         * Displays the name of the referenced Snippet.
         */
        private void setSnippetName() {
            snippetNameText.setText(snippetName);
        }

        @Override
        public void onClick(View view) {
            goToSnippet();
        }

        /**
         * Goes to SnippetActivity to allow the user to edit the Snippet referenced by the card.
         */
        private void goToSnippet() {
            Intent goToSnippetIntent = new Intent(context, SnippetActivity.class);

            goToSnippetIntent.putExtra(AppPreferences.WORLD_NAME, worldName);
            goToSnippetIntent.putExtra(AppPreferences.CATEGORY, category);
            goToSnippetIntent.putExtra(AppPreferences.ARTICLE_NAME, articleName);
            goToSnippetIntent.putExtra(AppPreferences.SNIPPET_NAME, snippetName);

            context.startActivity(goToSnippetIntent);
        }
    }

    private final ArrayList<String> snippets;
    private final Context context;
    private final String worldName;
    private final Category category;
    private final String articleName;

    /**
     * Instantiates a new SnippetsAdapter.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     */
    public SnippetsAdapter(Context context, String worldName, Category category, String articleName) {
        this.context = context;
        this.worldName = worldName;
        this.category = category;
        this.articleName = articleName;

        snippets = ExternalReader.getSnippetNames(context, worldName, category, articleName);
    }

    @Override
    public SnippetHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.snippet_card, parent, false);
        return new SnippetHolder(context, worldName, category, articleName, view);
    }

    @Override
    public void onBindViewHolder(SnippetHolder holder, int pos) {
        holder.bindSnippet(snippets.get(pos));
    }

    @Override
    public int getItemCount() {
        return snippets.size();
    }
}
