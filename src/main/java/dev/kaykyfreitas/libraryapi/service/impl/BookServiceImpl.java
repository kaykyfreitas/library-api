package dev.kaykyfreitas.libraryapi.service.impl;

import dev.kaykyfreitas.libraryapi.entity.Book;
import dev.kaykyfreitas.libraryapi.exception.BusinessException;
import dev.kaykyfreitas.libraryapi.repository.BookRepository;
import dev.kaykyfreitas.libraryapi.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    @Override
    public Book save(Book book) {
        if(bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("This ISBN already exists");
        }
        return bookRepository.save(book);
    }
}
