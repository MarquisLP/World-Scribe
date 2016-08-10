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
import android.widget.Toast;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.LinkedArticleList;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.activities.PlaceActivity;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying a Person's Residences.
 */
public class ResidencesAdapter extends RecyclerView.Adapter<ResidencesAdapter.ResidenceHolder>
implements ArticleLinkAdapter {

    /**
     * A ViewHolder containing a Residence Card. Clicking on it will navigate the user to a
     * PlaceActivity containing the specified Place's data. The card also contains an Erase
     * Button for deleting the selected Residence link (but not the Place itself).
     */
    public class ResidenceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView residenceCard;
        private final TextView residenceNameText;
        private final ImageButton deleteButton;
        private Residence residence;

        /**
         * Instantiates a new ResidenceHolder.
         * @param context The Context calling this method.
         * @param itemView The Residences Card that this ViewHolder will handle.
         */
        public ResidenceHolder(Context context, View itemView) {
            super(itemView);

            this.context = context;
            residenceCard = (CardView) itemView;
            residenceNameText = (TextView) residenceCard.findViewById(R.id.itemName);
            deleteButton = (ImageButton) residenceCard.findViewById(R.id.delete);

            residenceCard.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        /**
         * Stores Residence data and displays the name of the Place involved.
         * @param residence The Residence that is represented by this ViewHolder.
         */
        public void bindResidence(Residence residence) {
            this.residence = residence;
            setResidenceName();
        }

        /**
         * Displays the name of the referenced Residence.
         */
        private void setResidenceName() {
            residenceNameText.setText(residence.placeName);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == deleteButton.getId()) {
                confirmResidenceDeletion();
            } else {
                goToResidence();
            }
        }

        /**
         * Deletes the Residence represented by this ViewHolder upon user confirmation.
         */
        private void confirmResidenceDeletion() {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirmResidenceRemovalTitle,
                            residence.placeName))
                    .setMessage(context.getString(R.string.confirmResidenceRemoval,
                            residence.placeName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteResidence();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

        /**
         * Deletes the Residence represented by this ViewHolder.
         * If an error occurs during deletion, an error message is displayed.
         */
        public void deleteResidence() {
            boolean membershipWasDeleted = ExternalDeleter.deleteResidence(context, residence);
            if (membershipWasDeleted) {
                removeResidence(getAdapterPosition());
            } else {
                Toast.makeText(context, context.getString(R.string.deleteResidenceError,
                        residence.placeName),
                        Toast.LENGTH_SHORT).show();
            }
        }
        /**
         * Goes to PlaceActivity to allow the user to view and edit the specified Place of
         * Residence.
         */
        private void goToResidence() {
            Intent goToResidenceIntent = new Intent(context, PlaceActivity.class);

            goToResidenceIntent.putExtra(IntentFields.WORLD_NAME, residence.worldName);
            goToResidenceIntent.putExtra(IntentFields.CATEGORY, Category.Place);
            goToResidenceIntent.putExtra(IntentFields.ARTICLE_NAME, residence.placeName);

            context.startActivity(goToResidenceIntent);
        }

    }

    private final ArrayList<Residence> residences;
    private final Context context;

    /**
     * Instantiates a new ResidencesAdapter.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person this Adapter is attached to.
     */
    public ResidencesAdapter(Context context, String worldName, String personName) {
        this.context = context;

        residences = ExternalReader.getResidences(context, worldName, personName);
    }

    @Override
    public ResidenceHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.erasable_item_card, parent, false);
        return new ResidenceHolder(context, view);
    }

    @Override
    public void onBindViewHolder(ResidenceHolder holder, int pos) {
        holder.bindResidence(residences.get(pos));
    }

    @Override
    public int getItemCount() {
        return residences.size();
    }

    public LinkedArticleList getLinkedArticleList() {
        LinkedArticleList linkedArticleList = new LinkedArticleList();

        for (Residence residence : residences) {
            linkedArticleList.addArticle(Category.Place, residence.placeName);
        }

        return  linkedArticleList;
    }

    /**
     * Removes a Residence from the Residences list.
     * @param residencePosition The list index of the Residence to delete.
     */
    public void removeResidence(int residencePosition) {
        residences.remove(residencePosition);
        notifyItemRemoved(residencePosition);
    }

    /**
     * @return All Residences loaded into this Adapter.
     */
    public ArrayList<Residence> getResidences() {
        return residences;
    }

}
