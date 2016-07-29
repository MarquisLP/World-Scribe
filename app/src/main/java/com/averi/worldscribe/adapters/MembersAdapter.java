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
import com.averi.worldscribe.Member;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.PersonActivity;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying a Group's Members.
 */
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberHolder> {

    public class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView memberCard;
        private final TextView memberNameText;
        private final TextView memberRoleText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final String worldName;
        private Member member;

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
        }

        /**
         * Store Member data and use it to set the card's text.
         * @param member The Member represented by the card.
         */
        public void bindMember(Member member) {
            this.member = member;
            setMemberText();
        }

        /**
         * Set the card's text using data from the attached Member.
         */
        private void setMemberText() {
            memberNameText.setText(member.memberName);

            // A null String means no role or rank.
            if (member.memberRole == null) {
                memberRoleText.setVisibility(View.GONE);
            } else {
                memberRoleText.setText(context.getString(R.string.memberRoleText,
                        member.memberRole));
            }
        }

        @Override
        public void onClick(View view) {
            goToMember();
        }

        /**
         * Open the Member in a new PersonActivity.
         */
        private void goToMember() {
            Intent goToMemberIntent = new Intent(context, PersonActivity.class);

            goToMemberIntent.putExtra(IntentFields.WORLD_NAME, worldName);
            goToMemberIntent.putExtra(IntentFields.CATEGORY, Category.Person);
            goToMemberIntent.putExtra(IntentFields.ARTICLE_NAME, member.memberName);

            context.startActivity(goToMemberIntent);
        }
    }

    private final ArrayList<Member> members;
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

        members = ExternalReader.getMembers(context, worldName, groupName);
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.membership_card, parent, false);
        return new MemberHolder(context, worldName, view);
    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int pos) {
        holder.bindMember(members.get(pos));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }
}
