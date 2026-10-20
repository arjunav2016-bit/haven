package haven.pattern.state;

/**
 * Concrete State representing an enquiry in the initial PENDING state.
 */
public class PendingState implements EnquiryState {

    @Override
    public String getStatusName() {
        return "PENDING";
    }

    @Override
    public String getDescription() {
        return "Awaiting property warden review";
    }

    @Override
    public EnquiryState contact() {
        return new ContactedState();
    }

    @Override
    public EnquiryState confirm() {
        return new ConfirmedState();
    }

    @Override
    public EnquiryState decline() {
        return new DeclinedState();
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
