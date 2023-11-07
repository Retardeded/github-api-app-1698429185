package dev.danvega.social.model;

public class GitHubRepository {
    public GitHubRepository(String name, int stargazersCount) {
        this.name = name;
        this.stargazersCount = stargazersCount;
    }

    public GitHubRepository(String name) {
        this.name = name;
    }

    private String name;

    private int stargazersCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStargazersCount() {
        return stargazersCount;
    }

    public void setStargazersCount(int stargazersCount) {
        this.stargazersCount = stargazersCount;
    }
}