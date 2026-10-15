package haven.pattern.command;

import haven.model.Accommodation;
import haven.model.ModerationStatus;
import haven.repository.AccommodationRepository;
import java.util.Date;

/**
 * Concrete Command for moderating an accommodation listing (APPROVED, FLAGGED, REJECTED)
 * or toggling its featured status, with full undo support.
 */
public class ModerateListingCommand implements AdminCommand {
    private final AccommodationRepository accommodationRepository;
    private final String accommodationId;
    private final String newModerationStatus;
    private final Boolean newFeaturedState;
    private String previousModerationStatus;
    private boolean previousFeaturedState;
    private String accommodationName = "";
    private final Date executedAt;

    public ModerateListingCommand(AccommodationRepository accommodationRepository, String accommodationId, String newModerationStatus) {
        this(accommodationRepository, accommodationId, newModerationStatus, (Boolean) null);
    }

    public ModerateListingCommand(AccommodationRepository accommodationRepository, String accommodationId, ModerationStatus newModerationStatus) {
        this(accommodationRepository, accommodationId, newModerationStatus != null ? newModerationStatus.name() : (String) null, (Boolean) null);
    }

    public ModerateListingCommand(AccommodationRepository accommodationRepository, String accommodationId,
                                  ModerationStatus newModerationStatus, Boolean newFeaturedState) {
        this(accommodationRepository, accommodationId, newModerationStatus != null ? newModerationStatus.name() : (String) null, newFeaturedState);
    }

    public ModerateListingCommand(AccommodationRepository accommodationRepository, String accommodationId,
                                  String newModerationStatus, Boolean newFeaturedState) {
        this.accommodationRepository = accommodationRepository;
        this.accommodationId = accommodationId;
        this.newModerationStatus = newModerationStatus;
        this.newFeaturedState = newFeaturedState;
        this.executedAt = new Date();
    }

    @Override
    public void execute() {
        Accommodation acc = accommodationRepository.findById(accommodationId).orElse(null);
        if (acc != null) {
            this.previousModerationStatus = acc.getModerationStatus();
            this.previousFeaturedState = acc.isFeatured();
            this.accommodationName = acc.getName();
            if (newModerationStatus != null) {
                acc.setModerationStatus(newModerationStatus);
            }
            if (newFeaturedState != null) {
                acc.setFeatured(newFeaturedState);
            }
            accommodationRepository.save(acc);
        }
    }

    @Override
    public void undo() {
        Accommodation acc = accommodationRepository.findById(accommodationId).orElse(null);
        if (acc != null && previousModerationStatus != null) {
            acc.setModerationStatus(previousModerationStatus);
            acc.setFeatured(previousFeaturedState);
            accommodationRepository.save(acc);
        }
    }

    @Override
    public String getDescription() {
        String name = accommodationName.isEmpty() ? accommodationId : accommodationName;
        if (newFeaturedState != null) {
            return "Set listing '" + name + "' status=" + newModerationStatus + ", featured=" + newFeaturedState;
        }
        return "Set listing '" + name + "' status to " + newModerationStatus;
    }

    @Override
    public Date getExecutedAt() {
        return executedAt;
    }
}
