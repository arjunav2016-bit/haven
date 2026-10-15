package haven.pattern.state;

/**
 * Concrete State representing an enquiry where the owner has contacted the student.
 */
public class ContactedState implements EnquiryState {

    @Override
    public String getStatusName() {
        return "CONTACTED";
    }

    @Override
    public String getDescription() {
        return "Owner has contacted student for visit/verification";
    }

    @Override
    public EnquiryState contact() {
        return this;
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
    public boolean canTransitionTo(String targetStatus) {
        if (targetStatus == null) return false;
        String s = targetStatus.trim().toUpperCase();
        return "CONTACTED".equals(s) || "CONFIRMED".equals(s)
                || "DECLINED".equals(s) || "CANCELLED".equals(s);
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
