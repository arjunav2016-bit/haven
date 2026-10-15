package haven.pattern.state;

/**
 * State Pattern interface representing the lifecycle state of a student {@link haven.model.Enquiry}.
 * Encapsulates valid state transitions and prevents illegal status mutations.
 *
 * <p>Valid State Machine Transitions:</p>
 * <ul>
 *   <li>{@code PENDING} &rarr; {@code CONTACTED}, {@code CONFIRMED}, {@code DECLINED}, {@code CANCELLED}</li>
 *   <li>{@code CONTACTED} &rarr; {@code CONFIRMED}, {@code DECLINED}, {@code CANCELLED}</li>
 *   <li>{@code CONFIRMED} &rarr; {@code CANCELLED}</li>
 *   <li>{@code DECLINED} &rarr; (Terminal State)</li>
 *   <li>{@code CANCELLED} &rarr; (Terminal State)</li>
 * </ul>
 */
public interface EnquiryState {

    /**
     * Returns the canonical uppercase status identifier (e.g., "PENDING", "CONFIRMED").
     *
     * @return status identifier string
     */
    String getStatusName();

    /**
     * Returns a human-readable description of the current state.
     *
     * @return state description
     */
    String getDescription();

    /**
     * Transitions the enquiry to the CONTACTED state.
     *
     * @return the new {@link EnquiryState}
     * @throws IllegalStateException if the transition is not allowed from the current state
     */
    EnquiryState contact();

    /**
     * Transitions the enquiry to the CONFIRMED state.
     *
     * @return the new {@link EnquiryState}
     * @throws IllegalStateException if the transition is not allowed from the current state
     */
    EnquiryState confirm();

    /**
     * Transitions the enquiry to the DECLINED state.
     *
     * @return the new {@link EnquiryState}
     * @throws IllegalStateException if the transition is not allowed from the current state
     */
    EnquiryState decline();

    /**
     * Transitions the enquiry to the CANCELLED state.
     *
     * @return the new {@link EnquiryState}
     * @throws IllegalStateException if the transition is not allowed from the current state
     */
    EnquiryState cancel();

    /**
     * Checks whether a transition to the target status name is valid from this state.
     *
     * @param targetStatus target status string
     * @return true if allowed, false otherwise
     */
    boolean canTransitionTo(String targetStatus);

    /**
     * Transitions to the specified target status name if valid.
     *
     * @param targetStatus target status name
     * @return resulting {@link EnquiryState}
     * @throws IllegalStateException if the transition is invalid
     */
    default EnquiryState transitionTo(String targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status cannot be null.");
        }
        String normalized = targetStatus.trim().toUpperCase();
        if (normalized.equals(getStatusName())) {
            return this;
        }
        switch (normalized) {
            case "CONTACTED":
                return contact();
            case "CONFIRMED":
                return confirm();
            case "DECLINED":
                return decline();
            case "CANCELLED":
                return cancel();
            default:
                throw new IllegalStateException("Unsupported state transition from " + getStatusName() + " to " + normalized);
        }
    }

    /**
     * Indicates whether this state is terminal (no further transitions allowed).
     *
     * @return true if terminal
     */
    boolean isTerminal();
}
