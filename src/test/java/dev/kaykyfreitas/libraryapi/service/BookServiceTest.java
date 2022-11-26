package dev.kaykyfreitas.libraryapi.service;


import dev.kaykyfreitas.libraryapi.entity.Book;
import dev.kaykyfreitas.libraryapi.exception.BusinessException;
import dev.kaykyfreitas.libraryapi.repository.BookRepository;
import dev.kaykyfreitas.libraryapi.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class BookServiceTest {

    BookService bookService;
    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Should be able to save books.")
    void saveBookTest() {
        // Scenario
        var book = createValidBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(bookRepository.save(book)).thenReturn(Book.builder()
                .id(1L)
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build());

        // Execution
        var savedBook = (Book) bookService.save(book);

        // Verification
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
    }

    @Test
    @DisplayName("Should be able to throw BusinessException when try create a book with a duplicated ISBN")
    void throwBusinessExceptionForBookDuplicatedIsbn() throws Exception {
        var book = createValidBook();
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        Throwable exception = catchThrowable(() -> bookService.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("This ISBN already exists");

        verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Should be able to get a book by id")
    void getById() {
        var id = 1L;

        var book = createValidBook();
        book.setId(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        var foundBook = bookService.getById(id);

        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
    }

    @Test
    @DisplayName("Should be able to return empty when does not found a book by id")
    void bookNotFoundById() {
        var id = 1L;

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        var book = bookService.getById(id);

        assertThat(book).isEmpty();
    }

    @Test
    @DisplayName("Should be able to delete a book")
    void deleteBook() {
        var book = Book.builder().id(1L).build();

        Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Should not be able to delete a nonexistent book")
    void deleteInvalidBook() {
        var book = new Book();

        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        verify(bookRepository, never()).delete(book);
    }

    @Test
    @DisplayName("Should not be able to update a nonexistent book")
    void updateInvalidBook() {
        var book = new Book();

        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        verify(bookRepository, never()).save(book);
    }

    @Test
    @DisplayName("Should be able to update a book")
    void updateBook() {
        var id = 1L;
        var updatingBook = Book.builder().id(id).build();
        var book = createValidBook();
        book.setId(id);
        when(bookRepository.save(updatingBook)).thenReturn(book);

        var updatedBook = bookService.update(updatingBook);

        assertThat(updatedBook.getId()).isEqualTo(book.getId());
        assertThat(updatedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(updatedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(updatedBook.getAuthor()).isEqualTo(book.getAuthor());
    }

    @Test
    @DisplayName("Should be able to filter books by properties")
    void findBook() {
        var book = createValidBook();
        var pageRequest = PageRequest.of(0, 10);
        var list = Arrays.asList(book);
        var page = new PageImpl<Book>(list, pageRequest, 1);
        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        var result = bookService.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isZero();
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    private Book createValidBook() {
        return Book.builder().author("John Doe").title("The book").isbn("1234567").build();
    }

}
