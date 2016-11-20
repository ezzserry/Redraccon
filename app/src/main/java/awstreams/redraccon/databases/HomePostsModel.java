package awstreams.redraccon.databases;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

import awstreams.redraccon.models.NewsItem;

/**
 * Created by LENOVO on 15/11/2016.
 */
@Table(database = AppDatabase.class)
public class HomePostsModel extends BaseModel {

    public HomePostsModel() {
    }

    public HomePostsModel(String id, String slug, String title, String subtitle,  String category_id) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.subtitle = subtitle;
//        this.post_imageThumbnail = post_imageThumbnail;
//        this.post_imageFull = post_imageFull;
        this.category_id = category_id;

    }

    @PrimaryKey
    @Column
    @Unique
    String id; // package-private recommended, not required

    @Column
    @Unique
    String slug;

    @Column
    private String title;

    @Column
    private String subtitle;

    @Column
    private String category_id;

    @Column
    private String post_imageThumbnail;

    @Column
    private String post_imageFull;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getPost_imageThumbnail() {
        return post_imageThumbnail;
    }

    public void setPost_imageThumbnail(String post_imageThumbnail) {
        this.post_imageThumbnail = post_imageThumbnail;
    }

    public String getPost_imageFull() {
        return post_imageFull;
    }

    public void setPost_imageFull(String post_imageFull) {
        this.post_imageFull = post_imageFull;
    }


    public NewsItem getNewsItem() {
        return new NewsItem(id, slug, title, subtitle);
    }
}