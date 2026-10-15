package haven.pattern.state;

/**
 * Factory utility for resolving status strings into concrete {@link EnquiryState} instances.
 */
public final class EnquiryStateFactory {

    private EnquiryStateFactory() {}

    /**
     * Resolves a status string to its corresponding {@link EnquiryState} implementation.
     *
     * @param status status name (defaults to {@link PendingState} if null or unrecognized)
     * @return concrete {@link EnquiryState}
     */
    public static EnquiryState fromStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return new PendingState();
        }
        switch (status.trim().toUpperCase()) {
            case "CONTACTED":
                return new ContactedState();
            case "CONFIRMED":
                return new ConfirmedState();
            case "DECLINED":
                return new DeclinedState();
            case "CANCELLED":
                return new CancelledState();
            case "PENDING":
            default:
                return new PendingState();
        }
    }
}
