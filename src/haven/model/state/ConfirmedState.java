package haven.model.state;

/**
 * Concrete State representing an enquiry that has been confirmed by the property owner.
 * From CONFIRMED, a student may still cancel/withdraw their booking (restoring vacancy),
 * but it cannot be moved back to PENDING or CONTACTED.
 */
public class ConfirmedState implements EnquiryState {

    @Override
    public String getStatusName() {
        return "CONFIRMED";
    }

    @Override
    public String getDescription() {
        return "Booking confirmed and room reserved";
    }

    @Override
    public EnquiryState contact() {
        throw new IllegalStateException("Cannot mark as CONTACTED: Enquiry is already CONFIRMED.");
    }

    @Override
    public EnquiryState confirm() {
        return this;
    }

    @Override
    public EnquiryState decline() {
        throw new IllegalStateException("Cannot decline: Enquiry is already CONFIRMED. A student may only cancel/withdraw.");
    }

    @Override
    public EnquiryState cancel() {
        return new CancelledState();
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String toString() {
        return getStatusName();
    }
}
