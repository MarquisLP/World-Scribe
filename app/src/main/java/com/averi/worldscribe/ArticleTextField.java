package com.averi.worldscribe;

import android.content.Context;
import android.widget.EditText;

import com.averi.worldscribe.utilities.ExternalReader;

/**
 * Created by mark on 19/07/16.
 * An editable text field for describing some quality about a certain Article.
 */
public class ArticleTextField {
    /**
     * The name of this text field.
     */
    private String name;

    /**
     * The EditText that will allow the user to edit this text field.
     */
    private EditText editText;

    /**
     * The context this text field belongs to.
     */
    private Context context;

    /**
     * The name of the world of the Article this text field belongs to.
     */
    private String worldName;

    /**
     * The Category of the Article this text field belongs to.
     */
    private Category category;

    /**
     * The name of the Article this text field belongs to.
     */
    private String articleName;

    /**
     * Instantiates a new ArticleTextField.
     * @param name The name of this text field.
     * @param editText The EditText that will allow the user to edit this text field.
     * @param context The context this text field belongs to.
     * @param worldName The name of the world of the Article this text field belongs to.
     * @param category The Category of the Article this text field belongs to.
     * @param articleName The name of the Article this text field belongs to.
     */
    public ArticleTextField(String name, EditText editText, Context context, String worldName,
                            Category category, String articleName) {
        this.name = name;
        this.editText = editText;
        this.context = context;
        this.worldName = worldName;
        this.category = category;
        this.articleName = articleName;
    }

    /**
     * Loads data from the text file associated with this text field and display it in the EditText.
     * Displays empty contents instead if the text file doesn't exist or couldn't be read.
     */
    public void loadData() {
        editText.setText(ExternalReader.getArticleTextFieldData(context, worldName, category,
                articleName, name));
    }
}
