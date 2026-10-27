package haven.event;

import java.util.Date;

/**
 * Base class for all domain events in the HAVEN application.
 */
public abstract class HavenEvent {
    private final Date timestamp;

    public HavenEvent() {
        this.timestamp = new Date();
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }
}
