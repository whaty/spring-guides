package hello;

import org.springframework.data.repository.CrudRepository;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2019/1/3 15:22
 * @describtion: PersonRepository
 */
public interface PersonRepository extends CrudRepository<Person, Long> {

    Person findByName(String name);
}