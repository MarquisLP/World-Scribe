package com.averi.worldscribe.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.LinkedArticleList;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.activities.PersonActivity;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.IntentFields;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying a Place's Residents.
 */
public class ResidentsAdapter extends RecyclerView.Adapter<ResidentsAdapter.ResidentHolder>
implements ArticleLinkAdapter {

    /**
     * A ViewHolder containing a Resident Card. Clicking on it will navigate the user to a
     * PersonActivity containing the specified Person's data. The card also contains an Erase
     * Button for deleting the selected Resident link (but not the Person themself).
     */
    public class ResidentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView residentCard;
        private final TextView residentNameText;
        private final ImageButton deleteButton;
        private Residence residence;

        /**
         * Instantiates a new ResidentHolder.
         * @param context The Context calling this method.
         * @param itemView The Residents Card that this ViewHolder will handle.
         */
        public ResidentHolder(Context context, View itemView) {
            super(itemView);

            this.context = context;
            residentCard = (CardView) itemView;
            residentNameText = (TextView) residentCard.findViewById(R.id.itemName);
            deleteButton = (ImageButton) residentCard.findViewById(R.id.delete);

            residentCard.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        /**
         * Stores the Residence that this ViewHolder will represent and displays the name of the
         * resident involved.
         * @param residence The name of the Resident represented by the card.
         */
        public void bindResident(Residence residence) {
            this.residence = residence;
            setResidentName();
        }

        /**
         * Displays the name of the resident involved in the contained Residence.
         */
        private void setResidentName() {
            residentNameText.setText(residence.residentName);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == deleteButton.getId()) {
                confirmResidentDeletion();
            } else {
                goToResident();
            }
        }

        /**
         * Removes the Resident represented by this ViewHolder upon user confirmation.
         */
        private void confirmResidentDeletion() {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirmResidentRemovalTitle,
                            residence.residentName))
                    .setMessage(context.getString(R.string.confirmResidentRemoval,
                            residence.residentName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Remove the Resident.
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

        /**
         * Goes to PersonActivity to allow the user to view and edit the specified Resident.
         */
        private void goToResident() {
            Intent goToResidentIntent = new Intent(context, PersonActivity.class);

            goToResidentIntent.putExtra(IntentFields.WORLD_NAME, residence.worldName);
            goToResidentIntent.putExtra(IntentFields.CATEGORY, Category.Person);
            goToResidentIntent.putExtra(IntentFields.ARTICLE_NAME, residence.residentName);

            context.startActivity(goToResidentIntent);
        }
    }

    private final ArrayList<Residence> residentData;
    private final Context context;
    private final String worldName;
    private final String personName;

    /**
     * Instantiates a new ResidentsAdapter.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param placeActivity The name of the Place this Adapter is attached to.
     */
    public ResidentsAdapter(Context context, String worldName, String placeActivity) {
        this.context = context;
        this.worldName = worldName;
        this.personName = placeActivity;

        residentData = ExternalReader.getResidents(context, worldName, placeActivity);
    }

    @Override
    public ResidentHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.erasable_item_card, parent, false);
        return new ResidentHolder(context, view);
    }

    @Override
    public void onBindViewHolder(ResidentHolder holder, int pos) {
        holder.bindResident(residentData.get(pos));
    }

    @Override
    public int getItemCount() {
        return residentData.size();
    }

    public LinkedArticleList getLinkedArticleList() {
        LinkedArticleList linkedArticleList = new LinkedArticleList();

        for (Residence residence : residentData) {
            linkedArticleList.addArticle(Category.Person, residence.residentName);
        }

        return  linkedArticleList;
    }
}
