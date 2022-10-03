package dev.kaykyfreitas.libraryapi.repository;

import dev.kaykyfreitas.libraryapi.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Should be able to return true when exists a book whit the ISBN provided")
    public void returnTrueWhenIsbnExists() {
        String isbn = "1234567";
        Book book = Book.builder().author("John Doe").title("The book").isbn(isbn).build();
        testEntityManager.persist(book);

        boolean exists = bookRepository.existsByIsbn(isbn);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should be able to return false when does not exists a book whit the ISBN provided")
    public void returnFalseWhenIsbnDoesNotExists() {
        String isbn = "1234567";

        boolean exists = bookRepository.existsByIsbn(isbn);

        assertThat(exists).isFalse();
    }
}
