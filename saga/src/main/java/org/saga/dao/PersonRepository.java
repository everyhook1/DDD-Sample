package org.saga.dao;

import org.saga.entity.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findAll();

    <T extends Person> T save(T person);
}
