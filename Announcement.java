import java.time.LocalDateTime;

public class Announcement {
    private String title;
    private int productId;
    private String graphicsChip;
    private double memoryFrequency;
    private double coreFrequency;
    private int memoryCapacity;
    private int bitSizeMemoryBus;
    private String maxSupportedResolution;
    private int minRequiredBZCapacity;
    private String memoryType;
    private String producingCountry;
    private String supported3DApis;
    private String formFactor;
    private String coolingSystemType;
    private int guarantee;
    private double price;
    private double wholesalePrice;
    private int wholesaleQuantity;
    private String brand;
    private String description;
    private LocalDateTime date;
    private String creatorName;
    private int availableQuantity;

    public Announcement(String title, int productId, String graphicsChip,
                        double memoryFrequency, double coreFrequency, int memoryCapacity,
                        int bitSizeMemoryBus, String maxSupportedResolution,
                        int minRequiredBZCapacity, String memoryType, String producingCountry,
                        String supported3DApis, String formFactor, String coolingSystemType,
                        int guarantee, double price, double wholesalePrice, int wholesaleQuantity, String brand, String description, LocalDateTime date, String creatorName, int availableQuantity) {
        this.title = title;
        this.productId = productId;
        this.graphicsChip = graphicsChip;
        this.memoryFrequency = memoryFrequency;
        this.coreFrequency = coreFrequency;
        this.memoryCapacity = memoryCapacity;
        this.bitSizeMemoryBus = bitSizeMemoryBus;
        this.maxSupportedResolution = maxSupportedResolution;
        this.minRequiredBZCapacity = minRequiredBZCapacity;
        this.memoryType = memoryType;
        this.producingCountry = producingCountry;
        this.supported3DApis = supported3DApis;
        this.formFactor = formFactor;
        this.coolingSystemType = coolingSystemType;
        this.guarantee = guarantee;
        this.price = price;
        this.wholesalePrice = wholesalePrice;
        this.wholesaleQuantity = wholesaleQuantity;
        this.brand = brand;
        this.description = description;
        this.date = date;
        this.creatorName = creatorName;
        this.availableQuantity = availableQuantity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setGraphicsChip(String graphicsChip) {
        this.graphicsChip = graphicsChip;
    }

    public void setMemoryFrequency(double memoryFrequency) {
        this.memoryFrequency = memoryFrequency;
    }

    public void setCoreFrequency(double coreFrequency) {
        this.coreFrequency = coreFrequency;
    }

    public void setMemoryCapacity(int memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    public void setBitSizeMemoryBus(int bitSizeMemoryBus) {
        this.bitSizeMemoryBus = bitSizeMemoryBus;
    }

    public void setMaxSupportedResolution(String maxSupportedResolution) {
        this.maxSupportedResolution = maxSupportedResolution;
    }

    public void setMinRequiredBZCapacity(int minRequiredBZCapacity) {
        this.minRequiredBZCapacity = minRequiredBZCapacity;
    }

    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    public void setProducingCountry(String producingCountry) {
        this.producingCountry = producingCountry;
    }

    public void setSupported3DApis(String supported3DApis) {
        this.supported3DApis = supported3DApis;
    }

    public void setFormFactor(String formFactor) {
        this.formFactor = formFactor;
    }

    public void setCoolingSystemType(String coolingSystemType) {
        this.coolingSystemType = coolingSystemType;
    }

    public void setGuarantee(int guarantee) {
        this.guarantee = guarantee;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setWholesalePrice(double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public void setWholesaleQuantity(int wholesaleQuantity) {
        this.wholesaleQuantity = wholesaleQuantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getTitle() {
        return title;
    }

    public int getProductId() {
        return productId;
    }

    public String getGraphicsChip() {
        return graphicsChip;
    }

    public double getMemoryFrequency() {
        return memoryFrequency;
    }

    public double getCoreFrequency() {
        return coreFrequency;
    }

    public int getMemoryCapacity() {
        return memoryCapacity;
    }

    public int getBitSizeMemoryBus() {
        return bitSizeMemoryBus;
    }

    public String getMaxSupportedResolution() {
        return maxSupportedResolution;
    }

    public int getMinRequiredBZCapacity() {
        return minRequiredBZCapacity;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public String getProducingCountry() {
        return producingCountry;
    }

    public String getSupported3DApis() {
        return supported3DApis;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public String getCoolingSystemType() {
        return coolingSystemType;
    }

    public int getGuarantee() {
        return guarantee;
    }

    public double getPrice() {
        return price;
    }

    public double getWholesalePrice() {
        return wholesalePrice;
    }

    public int getWholesaleQuantity() {
        return wholesaleQuantity;
    }

    public String getBrand() { return brand; }

    public int getAvailableQuantity() { return availableQuantity; }

    public String getDescription() {
        return description;
    }

    public String getManagerUsername() {
        return creatorName;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "title='" + title + '\'' +
                ", productId=" + productId +
                ", graphicsChip='" + graphicsChip + '\'' +
                ", memoryFrequency=" + memoryFrequency +
                ", coreFrequency=" + coreFrequency +
                ", memoryCapacity=" + memoryCapacity +
                ", bitSizeMemoryBus=" + bitSizeMemoryBus +
                ", maxSupportedResolution='" + maxSupportedResolution + '\'' +
                ", minRequiredBZCapacity=" + minRequiredBZCapacity +
                ", memoryType='" + memoryType + '\'' +
                ", producingCountry='" + producingCountry + '\'' +
                ", supported3DApis='" + supported3DApis + '\'' +
                ", formFactor='" + formFactor + '\'' +
                ", coolingSystemType='" + coolingSystemType + '\'' +
                ", guarantee=" + guarantee +
                ", price=" + price +
                ", wholesalePrice=" + wholesalePrice +
                ", brand='" + brand + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", creatorName='" + creatorName + '\'' +
                ", availableQuantity ='" + availableQuantity + '\'' +
                '}';
    }
}
