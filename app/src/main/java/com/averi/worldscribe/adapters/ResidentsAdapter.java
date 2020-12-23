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
import com.averi.worldscribe.LinkedArticleList;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.activities.ArticleActivity;
import com.averi.worldscribe.activities.PersonActivity;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.IntentFields;

import java.util.ArrayList;
import java.util.Collections;

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
        private final ArticleActivity activity;
        private final CardView residentCard;
        private final TextView residentNameText;
        private final ImageButton deleteButton;
        private Residence residence;

        /**
         * Instantiates a new ResidentHolder.
         * @param activity The Context calling this method.
         * @param itemView The Residents Card that this ViewHolder will handle.
         */
        public ResidentHolder(ArticleActivity activity, View itemView) {
            super(itemView);

            this.activity = activity;
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
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.confirmResidentRemovalTitle,
                            residence.residentName))
                    .setMessage(activity.getString(R.string.confirmResidentRemoval,
                            residence.residentName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteResidence();
                        }})
                    .setNegativeButton(android.R.string.no, null).create();
            activity.showUnpausableAlertDialog(dialog);
        }

        /**
         * Deletes the Residence of the Person represented by this ViewHolder.
         * If an error occurs during deletion, an error message is displayed.
         */
        public void deleteResidence() {
            boolean membershipWasDeleted = ExternalDeleter.deleteResidence(activity, residence);
            if (membershipWasDeleted) {
                removeResident(getAdapterPosition());
            } else {
                Toast.makeText(activity, activity.getString(R.string.deleteResidenceError,
                        residence.placeName),
                        Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Goes to PersonActivity to allow the user to view and edit the specified Resident.
         */
        private void goToResident() {
            Intent goToResidentIntent = new Intent(activity, PersonActivity.class);

            goToResidentIntent.putExtra(IntentFields.WORLD_NAME, residence.worldName);
            goToResidentIntent.putExtra(IntentFields.CATEGORY, Category.Person);
            goToResidentIntent.putExtra(IntentFields.ARTICLE_NAME, residence.residentName);

            activity.removeFocus();
            activity.startActivity(goToResidentIntent);
        }
    }

    private final ArrayList<Residence> residentData;
    private final ArticleActivity activity;
    private final String worldName;
    private final String personName;

    /**
     * Instantiates a new ResidentsAdapter.
     * @param activity The Context calling this method.
     * @param worldName The name of the current World.
     * @param placeActivity The name of the Place this Adapter is attached to.
     */
    public ResidentsAdapter(ArticleActivity activity, String worldName, String placeActivity) {
        this.activity = activity;
        this.worldName = worldName;
        this.personName = placeActivity;
        this.residentData = new ArrayList<>();
    }

    @Override
    public ResidentHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.erasable_item_card, parent, false);
        return new ResidentHolder(activity, view);
    }

    @Override
    public void onBindViewHolder(ResidentHolder holder, int pos) {
        holder.bindResident(residentData.get(pos));
    }

    @Override
    public int getItemCount() {
        return residentData.size();
    }

    /**
     * Replace the list of Residents attached to this adapter.
     * @param newResidents The new list of Residents to attach to this adapter.
     */
    public void updateList(ArrayList<Residence> newResidents) {
        residentData.clear();
        residentData.addAll(newResidents);
        Collections.sort(residentData, new Residence.ByResidentNameComparator());
    }

    public LinkedArticleList getLinkedArticleList() {
        LinkedArticleList linkedArticleList = new LinkedArticleList();

        for (Residence residence : residentData) {
            linkedArticleList.addArticle(Category.Person, residence.residentName);
        }

        return  linkedArticleList;
    }

    /**
     * Removes a Resident from the Residents list.
     * @param residencePosition The list index of the Resident to delete.
     */
    public void removeResident(int residencePosition) {
        residentData.remove(residencePosition);
        notifyItemRemoved(residencePosition);
        notifyItemRangeChanged(residencePosition, getItemCount());

    }

    /**
     * @return All Residence data for the Residents loaded into this Adapter.
     */
    public ArrayList<Residence> getResidences() {
        return residentData;
    }

}
