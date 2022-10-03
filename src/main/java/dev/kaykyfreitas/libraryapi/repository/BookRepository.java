package dev.kaykyfreitas.libraryapi.repository;

import dev.kaykyfreitas.libraryapi.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
}
