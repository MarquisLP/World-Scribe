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
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.EditMembershipActivity;
import com.averi.worldscribe.activities.PersonActivity;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying Membership data for a Group's members.
 */
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberHolder>
implements ArticleLinkAdapter {

    public class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView memberCard;
        private final TextView memberNameText;
        private final TextView memberRoleText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final String worldName;
        private Membership membership;

        /**
         * Instantiate a new MemberHolder.
         * @param context The Context possessing this MemberHolder.
         * @param worldName The name of the current World.
         * @param memberCard The Member Card layout this object will hold.
         */
        public MemberHolder(Context context, String worldName, View memberCard) {
            super(memberCard);

            this.context = context;
            this.memberCard = (CardView) memberCard;
            memberNameText = (TextView) this.memberCard.findViewById(R.id.linkedArticleName);
            memberRoleText = (TextView) this.memberCard.findViewById(R.id.memberRole);
            editButton = (ImageButton) this.memberCard.findViewById(R.id.edit);
            deleteButton = (ImageButton) this.memberCard.findViewById(R.id.delete);
            this.worldName = worldName;

            this.memberCard.setOnClickListener(this);
            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        /**
         * Store the member's Membership data and use it to set the card's text.
         * @param membership The Membership containing the member's data.
         */
        public void bindMembership(Membership membership) {
            this.membership = membership;
            setMemberText();
        }

        /**
         * Set the card's text using data from the attached Membership.
         */
        private void setMemberText() {
            memberNameText.setText(membership.memberName);

            // An empty string means no role or rank.
            if (membership.memberRole.isEmpty()) {
                memberRoleText.setVisibility(View.GONE);
            } else {
                memberRoleText.setText(context.getString(R.string.memberRoleText,
                        membership.memberRole));
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == editButton.getId()) {
                editMembership();
            } else if (view.getId() == deleteButton.getId()) {
                confirmMemberRemoval();
            } else {
                goToMember();
            }
        }

        /**
         * Removes the Member represented by this ViewHolder from the Group upon user confirmation.
         */
        private void confirmMemberRemoval() {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirmMemberRemovalTitle,
                            membership.memberName))
                    .setMessage(context.getString(R.string.confirmMemberRemoval,
                            membership.memberName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteMember();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

        /**
         * Removes the Member represented by this ViewHolder from the Group.
         * If an error occurs during removal, an error message is displayed.
         */
        public void deleteMember() {
            boolean membershipWasDeleted = ExternalDeleter.deleteMembership(context, membership);
            if (membershipWasDeleted) {
                removeMember(getAdapterPosition());
            } else {
                Toast.makeText(context, context.getString(R.string.deleteMemberError,
                        membership.memberName),
                        Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Open the contained Membership in EditMembershipActivity for editing.
         */
        private void editMembership() {
            Intent editMembershipIntent = new Intent(context, EditMembershipActivity.class);
            editMembershipIntent.putExtra(IntentFields.MEMBERSHIP, membership);
            context.startActivity(editMembershipIntent);
        }

        /**
         * Open the Member in a new PersonActivity.
         */
        private void goToMember() {
            Intent goToMemberIntent = new Intent(context, PersonActivity.class);

            goToMemberIntent.putExtra(IntentFields.WORLD_NAME, worldName);
            goToMemberIntent.putExtra(IntentFields.CATEGORY, Category.Person);
            goToMemberIntent.putExtra(IntentFields.ARTICLE_NAME, membership.memberName);

            context.startActivity(goToMemberIntent);
        }
    }

    private final ArrayList<Membership> memberships;
    private final Context context;
    private final String worldName;

    /**
     * Instantiate a new MembersAdapter.
     * @param context The Context possessing this MembersAdapter.
     * @param worldName The name of the current World.
     * @param groupName The name of the Group whose Members are contained in this Adapter.
     */
    public MembersAdapter(Context context, String worldName, String groupName) {
        this.context = context;
        this.worldName = worldName;

        memberships = ExternalReader.getMembershipsInGroup(context, worldName, groupName);
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.membership_card, parent, false);
        return new MemberHolder(context, worldName, view);
    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int pos) {
        holder.bindMembership(memberships.get(pos));
    }

    @Override
    public int getItemCount() {
        return memberships.size();
    }

    public LinkedArticleList getLinkedArticleList() {
        LinkedArticleList linkedArticleList = new LinkedArticleList();

        for (Membership membership : memberships) {
            linkedArticleList.addArticle(Category.Person, membership.memberName);
        }

        return  linkedArticleList;
    }

    /**
     * Removes a Member from the Member list.
     * @param memberPosition The list index of the Member to remove.
     */
    public void removeMember(int memberPosition) {
        memberships.remove(memberPosition);
        notifyItemRemoved(memberPosition);
    }

    /**
     * @return All Membership data for the Members loaded into this Adapter.
     */
    public ArrayList<Membership> getMemberships() {
        return memberships;
    }

}
