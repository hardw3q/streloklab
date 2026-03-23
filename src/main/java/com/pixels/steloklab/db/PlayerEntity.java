package com.pixels.steloklab.db;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "wins", nullable = false)
    private int wins = 0;

    public PlayerEntity() {}

    public PlayerEntity(String username) {
        this.username = username;
        this.wins = 0;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
}
