package dev.kaykyfreitas.libraryapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kaykyfreitas.libraryapi.dto.BookDTO;
import dev.kaykyfreitas.libraryapi.entity.Book;
import dev.kaykyfreitas.libraryapi.exception.BusinessException;
import dev.kaykyfreitas.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class BookControllerTest {

    static String BOOK_API = "/api/books/";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Should be able to create books.")
    void createBookTest() throws Exception {

        var bookDTO = createNewBookDTO();
        var savedBook = Book.builder().id(10L).author("John Doe").title("The book").isbn("123").build();
        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

        var json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));

    }

    @Test
    @DisplayName("Should not be able to create invalid books.")
    void dontCreateInvalidBookTest() throws Exception {

        var json = new ObjectMapper().writeValueAsString(new Book());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Should not be able to create a book with a duplicated ISBN")
    void dontCreateBookDuplicatedIsbn() throws Exception {

        var bookDTO = createNewBookDTO();
        var json = new ObjectMapper().writeValueAsString(bookDTO);
        var errorMessage = "This ISBN already exists";
        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(errorMessage));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(errorMessage));
    }

    @Test
    @DisplayName("Should be able to return book details.")
    void returnBookDetails() throws Exception{
        var id = 1L;
        var book = Book.builder()
                .id(id)
                .author("John Doe")
                .title("The book")
                .isbn("123")
                .build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(book.getId()))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Should return resource not found when the requested book not exists")
    void bookNotFoundTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should be able to delete a book")
    void deleteBookTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return resource not found when does not found a book to delete")
    void deleteNonExistentBookTest() throws Exception {

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should be able to update a book")
    void updateBookTest() throws Exception {
        var id = 1L;
        var json = new ObjectMapper().writeValueAsString(createNewBookDTO());

        var book = Book.builder().id(id).title("Anything").author("Anyone").isbn("321").build();
        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        var updatedBook = Book.builder().id(id).author("John Doe").title("The book").isbn("321").build();
        BDDMockito.given(bookService.update(book)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBookDTO().getTitle()))
                .andExpect(jsonPath("author").value(createNewBookDTO().getAuthor()))
                .andExpect(jsonPath("isbn").value("321"));
    }

    @Test
    @DisplayName("Should return resource not found when does not found a book to update")
    void updateNonExistentBook() throws Exception {
        String json = new ObjectMapper().writeValueAsString(createNewBookDTO());

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should be able to filter books")
    void findBooks() throws Exception {
        var id = 1L;

        var book = Book.builder()
                .id(createNewBookDTO().getId())
                .title(createNewBookDTO().getTitle())
                .author(createNewBookDTO().getAuthor())
                .isbn(createNewBookDTO().getIsbn())
                .build();

        BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));


        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                                        book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect( jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

    private BookDTO createNewBookDTO() {
        return BookDTO.builder().author("John Doe").title("The book").isbn("123").build();
    }
}
