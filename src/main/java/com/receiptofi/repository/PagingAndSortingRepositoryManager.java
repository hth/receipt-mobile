/**
 *
 */
package com.receiptofi.repository;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author hitender
 * @since Mar 31, 2013 2:24:33 AM
 * {@link http://static.springsource.org/spring-data/data-mongo/docs/1.2.0.RELEASE/reference/htmlsingle/}
 */
public interface PagingAndSortingRepositoryManager <T extends Serializable> extends RepositoryManager<T> {

	Iterable<T> findAll(Sort sort);

    Page<T> findAll(Pageable pageable);
}
