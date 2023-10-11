package br.com.dkzit.project.post;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

public interface PostRepository extends ListCrudRepository<Post, Integer> {
}
