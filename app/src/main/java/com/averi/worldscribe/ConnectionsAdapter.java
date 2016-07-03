package com.averi.worldscribe;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.FileReader;
import java.util.ArrayList;

import tools.AppPreferences;
import tools.AttributeGetter;
import tools.ExternalReader;

/**
 * Created by mark on 02/07/16.
 */
public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionHolder> {

    public class ConnectionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView connectionCard;
        private final TextView articleRoleText;
        private final TextView connectedArticleNameText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final String worldName;
        private final String articleName;
        private Connection connection;

        public ConnectionHolder(Context context, String worldName, String articleName, View itemView) {
            super(itemView);

            this.context = context;
            connectionCard = (CardView) itemView;
            articleRoleText = (TextView) connectionCard.findViewById(R.id.mainArticleRole);
            connectedArticleNameText = (TextView) connectionCard.findViewById(R.id.otherArticleName);
            editButton = (ImageButton) connectionCard.findViewById(R.id.edit);
            deleteButton = (ImageButton) connectionCard.findViewById(R.id.delete);
            this.worldName = worldName;
            this.articleName = articleName;

            connectionCard.setOnClickListener(this);
        }

        public void bindConnection(Connection connection) {
            this.connection = connection;
            setConnectionText();
            // setCategoryIcon()
        }

        private void setConnectionText() {
            articleRoleText.setText(context.getResources().getString(
                    R.string.articleConnectionRoleSuffix, connection.articleRole));
            connectedArticleNameText.setText(connection.connectedArticleName);
        }

        @Override
        public void onClick(View view) {
            goToConnectedArticle();
        }

        private void goToConnectedArticle() {
            Intent goToArticleIntent;

            switch (connection.connectedArticleCategory) {
                case Person:
                    goToArticleIntent = new Intent(context, PersonActivity.class);
                    break;
                case Group:
                    goToArticleIntent = new Intent(context, GroupActivity.class);
                    break;
                case Place:
                    goToArticleIntent = new Intent(context, PlaceActivity.class);
                    break;
                case Item:
                    goToArticleIntent = new Intent(context, ItemActivity.class);
                    break;
                case Concept:
                default:
                    goToArticleIntent = new Intent(context, ConceptActivity.class);
            }

            goToArticleIntent.putExtra(AppPreferences.WORLD_NAME, worldName);
            goToArticleIntent.putExtra(AppPreferences.CATEGORY, connection.connectedArticleCategory);
            goToArticleIntent.putExtra(AppPreferences.ARTICLE_NAME, connection.connectedArticleName);

            context.startActivity(goToArticleIntent);
        }
    }

    private final ArrayList<Connection> connections;
    private final Context context;
    private final String worldName;
    private final String articleName;

    public ConnectionsAdapter(Context context, String worldName, Category category, String articleName) {
        this.context = context;
        this.worldName = worldName;
        this.articleName = articleName;

        connections = ExternalReader.getConnections(context, worldName, category, articleName);
    }

    @Override
    public ConnectionHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.connection_card, parent, false);
        return new ConnectionHolder(context, worldName, articleName, view);
    }

    @Override
    public void onBindViewHolder(ConnectionHolder holder, int pos) {
        holder.bindConnection(connections.get(pos));
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }
}
