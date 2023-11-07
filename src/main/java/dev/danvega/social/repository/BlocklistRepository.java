package dev.danvega.social.repository;

import dev.danvega.social.model.BlockedGithubRepo;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface BlocklistRepository extends CrudRepository<BlockedGithubRepo, Integer> {
    List<BlockedGithubRepo> findByUserId(Integer userId);
    BlockedGithubRepo findByRepositoryNameAndUserId(String repositoryName, Integer userId);
}