package haven.repository;

import haven.model.PropertyImage;
import java.util.List;

/**
 * Domain-specific contract for Property Image metadata persistence.
 * Extends the generic {@link Repository} interface.
 */
public interface ImageRepository extends Repository<PropertyImage, String> {

    List<PropertyImage> findByAccommodationId(String accommodationId);
}
