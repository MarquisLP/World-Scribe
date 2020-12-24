package com.averi.worldscribe.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.LinkedArticleList;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleActivity;
import com.averi.worldscribe.activities.ConceptActivity;
import com.averi.worldscribe.activities.EditConnectionActivity;
import com.averi.worldscribe.activities.GroupActivity;
import com.averi.worldscribe.activities.ItemActivity;
import com.averi.worldscribe.activities.PersonActivity;
import com.averi.worldscribe.activities.PlaceActivity;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.IntentFields;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mark on 02/07/16.
 */
public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionHolder>
implements ArticleLinkAdapter {

    public class ConnectionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ArticleActivity activity;
        private final CardView connectionCard;
        private final TextView articleRoleText;
        private final TextView connectedArticleNameText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final String worldName;
        private final String articleName;
        private Connection connection;

        public ConnectionHolder(ArticleActivity activity, String worldName, String articleName,
                                View itemView) {
            super(itemView);

            this.activity = activity;
            connectionCard = (CardView) itemView;
            articleRoleText = (TextView) connectionCard.findViewById(R.id.mainArticleRole);
            connectedArticleNameText = (TextView) connectionCard.findViewById(R.id.otherArticleName);
            editButton = (ImageButton) connectionCard.findViewById(R.id.edit);
            deleteButton = (ImageButton) connectionCard.findViewById(R.id.delete);
            this.worldName = worldName;
            this.articleName = articleName;

            connectionCard.setOnClickListener(this);
            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        public void bindConnection(Connection connection) {
            this.connection = connection;
            setConnectionText();
            // setCategoryIcon()
        }

        private void setConnectionText() {
            articleRoleText.setText(connection.articleRelation);
            connectedArticleNameText.setText(connection.connectedArticleName);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == editButton.getId()) {
                goToConnectionEditor();
            } else if (view.getId() == deleteButton.getId()) {
                deleteConnection();
            } else {
                goToConnectedArticle();
            }
        }

        /**
         * Open EditConnectionActivity so that the user can edit the Connection contained within
         * this ConnectionHolder.
         */
        private void goToConnectionEditor() {
            Intent editConnectionIntent = new Intent(activity, EditConnectionActivity.class);
            editConnectionIntent.putExtra(IntentFields.CONNECTION, connection);
            activity.startActivity(editConnectionIntent);
        }

        /**
         * Deletes the Connection represented in this ViewHolder.
         * If an error occurs during deletion, an error message is displayed.
         */
        private void deleteConnection() {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirmConnectionDeletionTitle,
                        connection.connectedArticleName))
                .setMessage(activity.getString(R.string.confirmConnectionDeletion,
                        connection.connectedArticleName))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean connectionWasDeleted = ExternalDeleter.deleteConnection(activity,
                                connection);
                        if ((connectionWasDeleted)) {
                            removeConnection(getAdapterPosition());
                        } else {
                            Toast.makeText(activity,
                                    activity.getString(R.string.deleteConnectionError,
                                            connection.connectedArticleName),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).create();
            activity.showUnpausableAlertDialog(dialog);
        }

        private void goToConnectedArticle() {
            Intent goToArticleIntent;

            switch (connection.connectedArticleCategory) {
                case Person:
                    goToArticleIntent = new Intent(activity, PersonActivity.class);
                    break;
                case Group:
                    goToArticleIntent = new Intent(activity, GroupActivity.class);
                    break;
                case Place:
                    goToArticleIntent = new Intent(activity, PlaceActivity.class);
                    break;
                case Item:
                    goToArticleIntent = new Intent(activity, ItemActivity.class);
                    break;
                case Concept:
                default:
                    goToArticleIntent = new Intent(activity, ConceptActivity.class);
            }

            goToArticleIntent.putExtra(IntentFields.WORLD_NAME, worldName);
            goToArticleIntent.putExtra(IntentFields.CATEGORY, connection.connectedArticleCategory);
            goToArticleIntent.putExtra(IntentFields.ARTICLE_NAME, connection.connectedArticleName);

            activity.removeFocus();
            activity.startActivity(goToArticleIntent);
        }
    }

    private final ArrayList<Connection> connections;
    private final ArticleActivity activity;
    private final String worldName;
    private final String articleName;

    public ConnectionsAdapter(ArticleActivity activity, String worldName, Category category, String articleName) {
        this.activity = activity;
        this.worldName = worldName;
        this.articleName = articleName;
        connections = new ArrayList<>();
    }

    @Override
    public ConnectionHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.connection_card, parent, false);
        return new ConnectionHolder(activity, worldName, articleName, view);
    }

    @Override
    public void onBindViewHolder(ConnectionHolder holder, int pos) {
        holder.bindConnection(connections.get(pos));
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    public LinkedArticleList getLinkedArticleList() {
        LinkedArticleList linkedArticleList = new LinkedArticleList();

        for (Connection connection : connections) {
            linkedArticleList.addArticle(connection.connectedArticleCategory,
                    connection.connectedArticleName);
        }

        return linkedArticleList;
    }

    /**
     * Replaces the Connections held by this adapter with new contents.
     * @param newConnections The list of Connections that this adapter will hold onto.
     */
    public void updateList(ArrayList<Connection> newConnections) {
        connections.clear();
        connections.addAll(newConnections);
        Collections.sort(connections);
    }

    /**
     * Removes a Connection from the Connections list.
     * @param connectionPosition The list index of the Connection to remove.
     */
    public void removeConnection(int connectionPosition) {
        connections.remove(connectionPosition);
        notifyItemRemoved(connectionPosition);
        notifyItemRangeChanged(connectionPosition, getItemCount());
    }

    /**
     * @return All Connections loaded into this Adapter.
     */
    public ArrayList<Connection> getConnections() {
        return connections;
    }

}
