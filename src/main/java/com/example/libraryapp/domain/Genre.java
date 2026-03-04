package com.example.libraryapp.domain;

import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "books")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();
}