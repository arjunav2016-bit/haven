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

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);

    long count();

    boolean existsById(ID id);
}
