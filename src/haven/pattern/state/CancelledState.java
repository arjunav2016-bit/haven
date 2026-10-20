package haven.pattern.state;

/**
 * Concrete Terminal State representing an enquiry cancelled/withdrawn by the student.
 */
public class CancelledState implements EnquiryState {

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }

    @Override
    public String getDescription() {
        return "Enquiry withdrawn by student; vacancy restored";
    }

    @Override
    public EnquiryState contact() {
        throw new IllegalStateException("Cannot contact: Enquiry was CANCELLED by the student.");
    }

    @Override
    public EnquiryState confirm() {
        throw new IllegalStateException("Cannot confirm an enquiry that was CANCELLED by the student.");
    }

    @Override
    public EnquiryState decline() {
        throw new IllegalStateException("Cannot decline an enquiry that was already CANCELLED.");
    }

    @Override
    public EnquiryState cancel() {
        return this;
    }



    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public String toString() {
        return getStatusName();
    }
}
