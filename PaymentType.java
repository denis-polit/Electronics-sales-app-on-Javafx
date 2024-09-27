public enum PaymentType {
    CASH("Cash"),
    CREDIT_CARD("Credit card"),
    BANK_TRANSFER("Bank transfer");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
