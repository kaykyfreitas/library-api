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
class BookRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Should be able to return true when exists a book whit the ISBN provided")
    void returnTrueWhenIsbnExists() {
        var book = createNewBook();
        testEntityManager.persist(book);

        var exists = bookRepository.existsByIsbn(book.getIsbn());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should be able to return false when does not exists a book whit the ISBN provided")
    void returnFalseWhenIsbnDoesNotExists() {
        var isbn = "1234567";

        var exists = bookRepository.existsByIsbn(isbn);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should be able to get a book by id")
    void findById() {
        var book = createNewBook();
        testEntityManager.persist(book);

        var foundBook = bookRepository.findById(book.getId());

        assertThat(foundBook).isPresent();
    }

    @Test
    @DisplayName("Should be able to save a book")
    void saveBook() {
        var book = createNewBook();

        var savedBook = bookRepository.save(book);

        assertThat(book.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should be able to delete a book")
    void deleteBook() {
        var book = createNewBook();
        testEntityManager.persist(book);

        var foundBook = testEntityManager.find(Book.class, book.getId());

        bookRepository.delete(foundBook);

        var deletedBook = testEntityManager.find(Book.class, book.getId());

        assertThat(deletedBook).isNull();
    }

    private static Book createNewBook() {
        return Book.builder().author("John Doe").title("The book").isbn("1234567").build();
    }

}
