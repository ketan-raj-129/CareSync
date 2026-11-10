package com.caresync.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object contract for CRUD operations across CareSync entities.
 *
 * @param <T>  Entity type extending BaseModel or domain entity
 * @param <ID> Primary key identifier type (e.g., Long, String)
 */
public interface GenericDao<T, ID> {

    /**
     * Finds an entity by its primary key identifier.
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all active entities of type T.
     */
    List<T> findAll();

    /**
     * Persists a new entity to the database and returns the persisted entity with generated identity keys.
     */
    T save(T entity);

    /**
     * Updates an existing entity in persistence.
     */
    boolean update(T entity);

    /**
     * Deletes an entity by its identifier.
     */
    boolean deleteById(ID id);
}

