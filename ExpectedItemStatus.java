public enum ExpectedItemStatus {
    UNDER_CONSIDERATION("Under consideration"),
    REQUALIFIED("Requalified"),
    COMPLETED_SUCCESSFULLY("Completed successfully"),
    COMPLETED_WITH_ERRORS("Completed with errors"),
    PAID_FOR("Paid for");

    private final String displayName;

    ExpectedItemStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}