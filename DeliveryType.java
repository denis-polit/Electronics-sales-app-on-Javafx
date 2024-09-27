public enum DeliveryType {
    COURIER_DELIVERY("Courier delivery"),
    MAIL_DELIVERY("Mail delivery"),
    SELF_PICKUP("Self pick-up");

    private final String displayName;

    DeliveryType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}