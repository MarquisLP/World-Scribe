package com.averi.worldscribe.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleActivity;
import com.averi.worldscribe.activities.SnippetActivity;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;

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
        private final ArticleActivity activity;
        private final CardView snippetCard;
        private final TextView snippetNameText;
        private final ImageButton renameButton;
        private final ImageButton deleteButton;
        private final String worldName;
        private final Category category;
        private final String articleName;
        private String snippetName;
        private SnippetsAdapter adapter;

        /**
         * Instantiates a new SnippetHolder.
         * @param adapter The SnippetsAdapter this ViewHolder belongs to.
         * @param activity The Activity calling this method.
         * @param worldName The name of the current World.
         * @param category The {@link Category} of the current Article.
         * @param articleName The name of the current Article.
         * @param itemView The Snippet Card that this ViewHolder will handle.
         */
        public SnippetHolder(SnippetsAdapter adapter, ArticleActivity activity, String worldName,
                             Category category, String articleName, View itemView) {
            super(itemView);

            this.adapter = adapter;
            this.activity = activity;
            snippetCard = (CardView) itemView;
            snippetNameText = (TextView) snippetCard.findViewById(R.id.itemName);
            renameButton = (ImageButton) snippetCard.findViewById(R.id.rename);
            deleteButton = (ImageButton) snippetCard.findViewById(R.id.delete);
            this.worldName = worldName;
            this.category = category;
            this.articleName = articleName;

            snippetCard.setOnClickListener(this);
            renameButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
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
            if (view.getId() == renameButton.getId()) {
                showRenameDialog();
            } else if (view.getId() == deleteButton.getId()) {
                deleteSnippet();
            } else {
                goToSnippet();
            }
        }

        /**
         * Shows the dialog for renaming the Snippet represented by this ViewHolder.
         */
        private void showRenameDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View content = inflater.inflate(R.layout.rename_snippet_dialog, null);

            final EditText nameField = (EditText) content.findViewById(R.id.snippetName);
            nameField.setText(snippetName);

            final AlertDialog dialog = builder.setView(content)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) { }
                    })
                    .setNegativeButton(android.R.string.cancel, null).create();
            activity.showUnpausableAlertDialog(dialog);

            // Handle onClick here to prevent the dialog from closing if the user enters
            // an invalid name.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                    new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String newName = nameField.getText().toString();

                    Boolean renameWasSuccessful = renameSnippet(newName);
                    if (renameWasSuccessful) {
                        dialog.dismiss();
                        snippetName = newName;
                        setSnippetName();
                    }
                }
            });
        }

        /**
         * <p>
         *     Renames the Snippet associated with this ViewHolder, only if the new name is
         *     non-empty and is not already in use by an existing Snippet.
         * </p>
         * <p>
         *     If either of these conditions are untrue, an error message is displayed.
         * </p>
         * @param newName The new name to give the Snippet.
         */
        private boolean renameSnippet(String newName) {
            boolean renameWasSuccessful;

            if (newName.isEmpty()) {
                Toast.makeText(activity, R.string.emptySnippetNameError, Toast.LENGTH_SHORT).show();
                renameWasSuccessful = false;
            } else if (newName.equals(snippetName)) {   // Name was not changed.
                renameWasSuccessful = true;
            } else if (ExternalReader.snippetExists(activity, worldName, category, articleName,
                    newName)) {
                Toast.makeText(activity,
                    activity.getString(R.string.snippetExistsError, newName, articleName),
                    Toast.LENGTH_SHORT).show();
                renameWasSuccessful = false;
            } else {
                if (ExternalWriter.renameSnippet(activity, worldName, category, articleName,
                        snippetName, newName)) {
                    renameWasSuccessful = true;
                } else {
                    renameWasSuccessful = false;
                    Toast.makeText(activity,
                            activity.getString(R.string.renameSnippetError, snippetName),
                            Toast.LENGTH_SHORT).show();
                }
            }

            return renameWasSuccessful;
        }

        /**
         * Deletes the Snippet referenced by this ViewHolder upon user confirmation.
         * If an error occurs during deletion, an error message is displayed.
         */
        private void deleteSnippet() {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.confirmSnippetDeletionTitle, snippetName))
                    .setMessage(activity.getString(R.string.confirmSnippetDeletion, snippetName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            boolean snippetWasDeleted = ExternalDeleter.deleteSnippet(activity,
                                    worldName, category, articleName, snippetName);
                            if (!(snippetWasDeleted)) {
                                Toast.makeText(activity,
                                        activity.getString(R.string.deleteSnippetError, snippetName),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                adapter.removeSnippet(getAdapterPosition());
                            }
                        }})
                    .setNegativeButton(android.R.string.no, null).create();
            activity.showUnpausableAlertDialog(dialog);
        }

        /**
         * Goes to SnippetActivity to allow the user to edit the Snippet referenced by the card.
         */
        private void goToSnippet() {
            Intent goToSnippetIntent = new Intent(activity, SnippetActivity.class);

            goToSnippetIntent.putExtra(IntentFields.WORLD_NAME, worldName);
            goToSnippetIntent.putExtra(IntentFields.CATEGORY, category);
            goToSnippetIntent.putExtra(IntentFields.ARTICLE_NAME, articleName);
            goToSnippetIntent.putExtra(IntentFields.SNIPPET_NAME, snippetName);

            activity.removeFocus();
            activity.startActivity(goToSnippetIntent);
        }
    }

    private final ArrayList<String> snippets;
    private final Activity activity;
    private final String worldName;
    private final Category category;
    private final String articleName;

    /**
     * Instantiates a new SnippetsAdapter.
     * @param activity The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     */
    public SnippetsAdapter(ArticleActivity activity, String worldName, Category category, String articleName) {
        this.activity = activity;
        this.worldName = worldName;
        this.category = category;
        this.articleName = articleName;

        snippets = ExternalReader.getSnippetNames(activity, worldName, category, articleName);
    }

    @Override
    public SnippetHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.snippet_card, parent, false);
        return new SnippetHolder(this, (ArticleActivity) activity, worldName, category, articleName, view);
    }

    @Override
    public void onBindViewHolder(SnippetHolder holder, int pos) {
        holder.bindSnippet(snippets.get(pos));
    }

    @Override
    public int getItemCount() {
        return snippets.size();
    }

    /**
     * Remove a Snippet from the list.
     * @param snippetPosition The list index of the Snippet to remove.
     */
    public void removeSnippet(int snippetPosition) {
        snippets.remove(snippetPosition);
        notifyItemRemoved(snippetPosition);
    }

    /**
     * @return The names of all Snippets loaded into this Adapter.
     */
    public ArrayList<String> getSnippetNames() {
        return snippets;
    }

}
