package com.averi.worldscribe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.LinkedArticleList;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.StringListAdapter;
import com.averi.worldscribe.adapters.StringListContext;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AttributeGetter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.IntentFields;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This Activity is shown whenever the user has to select a pre-existing Article from the current
 * World. For example, when choosing the other Article to link when creating/editing a Connection.
 * </p>
 *
 * <p>
 * The Intent used to create this Activity can optionally include the "category" field. If it is
 * specified, then this Activity will only show Articles of that {@link Category}, and the BottomBar
 * will be hidden so that the user cannot switch Categories. This is useful if, for example, the
 * selection specifically requires a Person.
 * <br />
 * If a Category isn't specified, then the user is free to choose from any Category, using the
 * BottomBar to navigate between Categories of Articles.
 * </p>
 *
 * <p>
 * The Category of the Article who will receive the link must still be passed, though the Intent
 * must pass it via the "mainArticleCategory" field instead, to avoid mix-ups with the field above.
 * Similarly, the name of that Article must also be passed via "mainArticleName" instead of
 * "articleName".
 * </p>
 */
public class SelectArticleActivity extends BackButtonActivity implements StringListContext {

    /**
     * The initial Category of Articles to show if any Category can be chosen from.
     */
    public static final Category DEFAULT_CATEGORY = Category.Person;

    private Toolbar appBar;
    private RecyclerView recyclerView;
    private String worldName;
    private Category mainArticleCategory;
    private String mainArticleName;
    private Category category;
    private LinearLayout bottomBarLayout;
    private LinearLayout peopleButton;
    private LinearLayout groupsButton;
    private LinearLayout placesButton;
    private LinearLayout itemsButton;
    private LinearLayout conceptsButton;
    private TextView textEmpty;
    private LinkedArticleList existingLinks;
    private List<String> articleNames = new ArrayList<>();
    private boolean canChooseOneCategoryOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appBar = (Toolbar) findViewById(R.id.my_toolbar);
        textEmpty = (TextView) findViewById(R.id.empty);
        bottomBarLayout = (LinearLayout) findViewById(R.id.bottomBar);
        if (bottomBarLayout != null) {
            peopleButton = (LinearLayout) bottomBarLayout.findViewById(R.id.peopleButton);
            groupsButton = (LinearLayout) bottomBarLayout.findViewById(R.id.groupsButton);
            placesButton = (LinearLayout) bottomBarLayout.findViewById(R.id.placesButton);
            itemsButton = (LinearLayout) bottomBarLayout.findViewById(R.id.itemsButton);
            conceptsButton = (LinearLayout) bottomBarLayout.findViewById(R.id.conceptsButton);
            setBottomBarListeners();
        }
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        setUpRecyclerView();

        Intent startupIntent = getIntent();
        existingLinks = (LinkedArticleList) startupIntent.getSerializableExtra(
                IntentFields.EXISTING_LINKS);
        canChooseOneCategoryOnly = startupIntent.hasExtra(IntentFields.CATEGORY);
        worldName = startupIntent.getStringExtra(IntentFields.WORLD_NAME);
        mainArticleCategory=  (Category) startupIntent.getSerializableExtra(
                IntentFields.MAIN_ARTICLE_CATEGORY);
        mainArticleName = startupIntent.getStringExtra(IntentFields.MAIN_ARTICLE_NAME);
        setCategory(getInitialCategory(startupIntent));

        if (canChooseOneCategoryOnly) {
            bottomBarLayout.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams params = (
                    ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            params.bottomMargin = 0;
        }

        setAppBar();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_select_article;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected void setAppBar() {
        if (canChooseOneCategoryOnly) {
            appBar.setTitle(getString(R.string.selectArticleTitle, category.name()));
        } else {
            appBar.setTitle(getString(R.string.selectArticleTitle, getString(R.string.article)));
        }

        setSupportActionBar(appBar);

        super.setAppBar();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_article_menu, menu);

        ActivityUtilities.setUpSearchViewAppearance(this, menu, getString(R.string.searchHint));
        ActivityUtilities.setSearchViewFiltering(menu,
                (StringListAdapter) this.recyclerView.getAdapter());

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Set up the RecyclerView to display the list of Article names.
     */
    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(new StringListAdapter(this, new ArrayList<String>()));
    }

    /**
     * Returns the initial Category of Articles that will be displayed.
     * If an Article of any Category can be selected, return the default Category.
     * If only a specific Category can be selected from, that Category is returned.
     * @param startupIntent The Intent passed to this Activity during creation.
     * @return The initial Category to display in this Activity.
     */
    private Category getInitialCategory(Intent startupIntent) {
        if (canChooseOneCategoryOnly) {
            return ((Category) startupIntent.getSerializableExtra(IntentFields.CATEGORY));
        } else {
            return DEFAULT_CATEGORY;
        }
    }

    /**
     * Sets the Category to display in this Activty, and updates the BottomBar and RecyclerView
     * to reflect this.
     * @param newCategory The Category to display.
     */
    private void setCategory(Category newCategory) {
        category = newCategory;
        if (!(canChooseOneCategoryOnly)) {
            unhighlightAllBottomBarButtons();
            highlightBottomBarButton(newCategory);
        }
        populateList(newCategory);
    }

    /**
     * Changes the background color of all BottomBar buttons to their non-selected colour.
     */
    private void unhighlightAllBottomBarButtons() {
        int nonSelectedButtonColor = AttributeGetter.getColorAttribute(this, R.attr.colorPrimary);
        peopleButton.setBackgroundColor(nonSelectedButtonColor);
        groupsButton.setBackgroundColor(nonSelectedButtonColor);
        placesButton.setBackgroundColor(nonSelectedButtonColor);
        itemsButton.setBackgroundColor(nonSelectedButtonColor);
        conceptsButton.setBackgroundColor(nonSelectedButtonColor);
    }

    /**
     * Highlights the button on the Bottom Bar representing a specific {@link Category}.
     * @param highlightedCategory The Article Category that will have its button highlighted.
     */
    private void highlightBottomBarButton(Category highlightedCategory) {
        LinearLayout categoryButton;

        switch (highlightedCategory) {
            case Person:
                categoryButton = peopleButton;
                break;
            case Group:
                categoryButton = groupsButton;
                break;
            case Place:
                categoryButton = placesButton;
                break;
            case Item:
                categoryButton = itemsButton;
                break;
            case Concept:
            default:
                categoryButton = conceptsButton;
        }

        int selectedButtonColor = AttributeGetter.getColorAttribute(this, R.attr.colorPrimaryDark);
        categoryButton.setBackgroundColor(selectedButtonColor);
    }

    /**
     * Updates the list to show the names of Articles from a specific Category.
     * If the Category has no Articles, a text message will notify that the list is empty.
     * @param listCategory The Category of Articles that will be displayed in the list.
     */
    private void populateList(Category listCategory) {
        articleNames.clear();
        articleNames.addAll(getLinkableArticleNames(listCategory));

        StringListAdapter adapter = (StringListAdapter) recyclerView.getAdapter();
        adapter.updateList(new ArrayList<>(articleNames));
        adapter.notifyDataSetChanged();

        if (articleNames.isEmpty()) {
            if (textEmpty != null) {
                textEmpty.setVisibility(View.VISIBLE);
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            if (textEmpty != null) {
                textEmpty.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the names of all Articles in the current Category, excluding the main Article and
     * those already linked to the main Article.
     * @param listCategory  The Category of the Articles that will be retrieved.
     */
    private List<String> getLinkableArticleNames(Category listCategory) {
        List<String> linkableNames = ExternalReader.getArticleNamesInCategory(
                this, worldName, listCategory);

        linkableNames.removeAll(existingLinks.getAllLinksInCategory(listCategory));

        if (category == mainArticleCategory) {
            linkableNames.remove(mainArticleName);
        }

        return linkableNames;
    }

    /**
     * Sets listeners for the Bottom Bar buttons so that Activity displays the selected button's
     * Category when clicked.
     */
    private void setBottomBarListeners() {
        peopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCategory(Category.Person);
            }
        });
        groupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCategory(Category.Group);
            }
        });
        placesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCategory(Category.Place);
            }
        });
        itemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCategory(Category.Item);
            }
        });
        conceptsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCategory(Category.Concept);
            }
        });
    }

    /**
     * Return the name and {@link Category} of the selected Article to the Activity that called this
     * Activity.
     * @param itemText The name of the selected Article.
     */
    public void respondToListItemSelection(String itemText) {
        Intent data = new Intent();
        data.putExtra(IntentFields.CATEGORY, category);
        data.putExtra(IntentFields.ARTICLE_NAME, itemText);
        setResult(RESULT_OK, data);
        finish();
    }
}
