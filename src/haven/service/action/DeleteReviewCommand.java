package haven.service.action;

import haven.model.Review;
import haven.repository.ReviewRepository;
import java.util.Date;

/**
 * Concrete Command for removing a student review with undo restoration.
 */
public class DeleteReviewCommand implements AdminCommand {
    private final ReviewRepository reviewRepository;
    private final Review reviewToDelete;
    private final Date executedAt;

    public DeleteReviewCommand(ReviewRepository reviewRepository, Review reviewToDelete) {
        this.reviewRepository = reviewRepository;
        this.reviewToDelete = reviewToDelete;
        this.executedAt = new Date();
    }

    @Override
    public void execute() {
        if (reviewToDelete != null && reviewToDelete.getId() != null) {
            reviewRepository.deleteById(reviewToDelete.getId());
        }
    }

    @Override
    public void undo() {
        if (reviewToDelete != null) {
            reviewRepository.save(reviewToDelete);
        }
    }

    @Override
    public String getDescription() {
        return "Delete review by @" + (reviewToDelete != null ? reviewToDelete.getUsername() : "unknown");
    }

    @Override
    public Date getExecutedAt() {
        return executedAt;
    }
}
