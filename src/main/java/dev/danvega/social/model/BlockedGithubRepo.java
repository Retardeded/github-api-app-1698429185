package dev.danvega.social.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class BlockedGithubRepo {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @Column(nullable = false)
    private String repositoryName; // Change the field to store repository names

    @Column(nullable = false)
    private Integer userId;

    @Temporal(TemporalType.DATE)
    private Date createdAt;

    public BlockedGithubRepo() {
        // Default constructor
    }

    public BlockedGithubRepo(String repositoryName, Integer userId) {
        this.repositoryName = repositoryName;
        this.userId = userId;
        this.createdAt = new Date();
    }

    @Override
    public String toString() {
        return "BlockedGithubRepo{" +
                "id=" + id +
                ", repositoryName='" + repositoryName + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}
