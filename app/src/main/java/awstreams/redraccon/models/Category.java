package awstreams.redraccon.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LENOVO on 27/09/2016.
 */
public class Category {
    @SerializedName("id")
    private String id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
