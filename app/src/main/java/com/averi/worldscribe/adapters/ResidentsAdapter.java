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
import com.averi.worldscribe.activities.PersonActivity;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying a Place's Residents.
 */
public class ResidentsAdapter extends RecyclerView.Adapter<ResidentsAdapter.ResidentHolder> {

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
        private final String worldName;
        private String residentName;

        /**
         * Instantiates a new ResidentHolder.
         * @param context The Context calling this method.
         * @param worldName The name of the current World.
         * @param personName The name of the current Article.
         * @param itemView The Residents Card that this ViewHolder will handle.
         */
        public ResidentHolder(Context context, String worldName, String personName, View itemView) {
            super(itemView);

            this.context = context;
            residentCard = (CardView) itemView;
            residentNameText = (TextView) residentCard.findViewById(R.id.itemName);
            deleteButton = (ImageButton) residentCard.findViewById(R.id.delete);
            this.worldName = worldName;
            this.residentName = personName;

            residentCard.setOnClickListener(this);
        }

        /**
         * Stores the name of the Resident and displays it.
         * @param residentName The name of the Resident represented by the card.
         */
        public void bindResident(String residentName) {
            this.residentName = residentName;
            setSnippetName();
        }

        /**
         * Displays the name of the referenced Resident.
         */
        private void setSnippetName() {
            residentNameText.setText(residentName);
        }

        @Override
        public void onClick(View view) {
            goToResident();
        }

        /**
         * Goes to PersonActivity to allow the user to view and edit the specified Resident.
         */
        private void goToResident() {
            Intent goToResidentIntent = new Intent(context, PersonActivity.class);

            goToResidentIntent.putExtra(AppPreferences.WORLD_NAME, worldName);
            goToResidentIntent.putExtra(AppPreferences.CATEGORY, Category.Person);
            goToResidentIntent.putExtra(AppPreferences.ARTICLE_NAME, residentName);

            context.startActivity(goToResidentIntent);
        }
    }

    private final ArrayList<String> residents;
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

        residents = ExternalReader.getResidents(context, worldName, placeActivity);
    }

    @Override
    public ResidentHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.erasable_item_card, parent, false);
        return new ResidentHolder(context, worldName, personName, view);
    }

    @Override
    public void onBindViewHolder(ResidentHolder holder, int pos) {
        holder.bindResident(residents.get(pos));
    }

    @Override
    public int getItemCount() {
        return residents.size();
    }
}
