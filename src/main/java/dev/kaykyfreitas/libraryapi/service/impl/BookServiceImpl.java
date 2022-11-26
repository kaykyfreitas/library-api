package dev.kaykyfreitas.libraryapi.service.impl;

import dev.kaykyfreitas.libraryapi.entity.Book;
import dev.kaykyfreitas.libraryapi.exception.BusinessException;
import dev.kaykyfreitas.libraryapi.repository.BookRepository;
import dev.kaykyfreitas.libraryapi.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    @Override
    public Book save(Book book) {
        if(bookRepository.existsByIsbn(book.getIsbn()))
            throw new BusinessException("This ISBN already exists");
        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(Objects.isNull(book) || Objects.isNull(book.getId()))
            throw new IllegalArgumentException("Book id can't be null");
        bookRepository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(Objects.isNull(book) || Objects.isNull(book.getId()))
            throw new IllegalArgumentException("Book id can't be null");
        return bookRepository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter,
                                    ExampleMatcher
                                            .matching()
                                            .withIgnoreCase()
                                            .withIgnoreNullValues()
                                            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING ));

        return bookRepository.findAll(example, pageRequest);
    }

}
