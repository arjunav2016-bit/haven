package haven.pattern.state;

/**
 * Concrete Terminal State representing an enquiry declined by the property owner.
 */
public class DeclinedState implements EnquiryState {

    @Override
    public String getStatusName() {
        return "DECLINED";
    }

    @Override
    public String getDescription() {
        return "Enquiry declined by property owner; vacancy restored";
    }

    @Override
    public EnquiryState contact() {
        throw new IllegalStateException("Cannot contact: Enquiry has already been DECLINED.");
    }

    @Override
    public EnquiryState confirm() {
        throw new IllegalStateException("Cannot confirm an enquiry that has already been DECLINED.");
    }

    @Override
    public EnquiryState decline() {
        return this;
    }

    @Override
    public EnquiryState cancel() {
        throw new IllegalStateException("Cannot cancel an enquiry that has already been DECLINED.");
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
