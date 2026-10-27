package haven.service;

import haven.model.Accommodation;
import haven.model.decorator.FeaturedAccommodationDecorator;
import haven.service.filter.AccommodationFilterCriteria;
import haven.service.filter.FilteredAccommodationIterator;
import haven.repository.AccommodationRepository;
import haven.repository.RepositoryProvider;
import haven.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service Layer component for managing student accommodations.
 * Encapsulates domain business logic, Decorator attachment for featured stays,
 * and lazy search/filtering via {@link FilteredAccommodationIterator}.
 */
public class AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final ReviewRepository reviewRepository;

    public AccommodationService() {
        this(RepositoryProvider.getAccommodationRepository(), RepositoryProvider.getReviewRepository());
    }

    public AccommodationService(AccommodationRepository accommodationRepository, ReviewRepository reviewRepository) {
        this.accommodationRepository = accommodationRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<Accommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }

    public Optional<Accommodation> getAccommodationById(String id) {
        return accommodationRepository.findById(id);
    }

    /**
     * Traverses and filters accommodations using the custom {@link FilteredAccommodationIterator}.
     */
    public List<Accommodation> searchAndFilter(AccommodationFilterCriteria criteria) {
        List<Accommodation> all = accommodationRepository.findAll();
        FilteredAccommodationIterator iterator = new FilteredAccommodationIterator(all, criteria, reviewRepository);
        return iterator.toList();
    }

    /**
     * Wraps an accommodation in a {@link FeaturedAccommodationDecorator} if marked featured,
     * applying promotional discount logic without modifying concrete domain classes.
     */
    public Accommodation getDecoratedStay(Accommodation raw) {
        if (raw == null) return null;
        if (raw instanceof FeaturedAccommodationDecorator) return raw;
        if (raw.isFeatured()) {
            return new FeaturedAccommodationDecorator(raw, "FEATURED", 10.0);
        }
        return raw;
    }

    public boolean decrementVacancy(String id) {
        return accommodationRepository.decrementVacancy(id);
    }

    public void updateVacancies(String id, int vacancies) {
        accommodationRepository.updateVacancies(id, vacancies);
    }

    public Accommodation saveAccommodation(Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }

    public boolean deleteAccommodation(String id) {
        return accommodationRepository.deleteById(id);
    }
}
