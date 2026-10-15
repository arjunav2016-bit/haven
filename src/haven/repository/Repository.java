package haven.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic Base Repository demonstrating Java Generics & CRUD Abstraction.
 *
 * @param <T>  The domain entity type
 * @param <ID> The primary key / identifier type
 */
public interface Repository<T, ID> {

    /**
     * Persists or updates the given entity in the data store.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Retrieves an entity by its unique identifier.
     *
     * @param id the entity identifier
     * @return Optional containing entity if found, empty otherwise
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities stored in the repository.
     *
     * @return list of all entities
     */
    List<T> findAll();

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the entity identifier
     * @return true if deleted, false if not found
     */
    boolean deleteById(ID id);

    /**
     * Returns the total count of entities in the store.
     *
     * @return count of entities
     */
    long count();

    /**
     * Checks if an entity exists by its identifier.
     *
     * @param id the entity identifier
     * @return true if entity exists, false otherwise
     */
    boolean existsById(ID id);
}
