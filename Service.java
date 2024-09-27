public class Service {
    private int id;
    private String title;
    private String description;
    private String iconPath;

    public Service(int id, String title, String description, String iconPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }
}