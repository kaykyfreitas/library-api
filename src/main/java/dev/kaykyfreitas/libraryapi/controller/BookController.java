package dev.kaykyfreitas.libraryapi.controller;

import dev.kaykyfreitas.libraryapi.dto.BookDTO;
import dev.kaykyfreitas.libraryapi.entity.Book;
import dev.kaykyfreitas.libraryapi.exception.ApiErrors;
import dev.kaykyfreitas.libraryapi.exception.BusinessException;
import dev.kaykyfreitas.libraryapi.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/")
public class BookController {

    private final BookService bookService;

    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        var entity = modelMapper.map(dto, Book.class);
        entity = (Book) bookService.save(entity);
        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("/{id}")
    public BookDTO getById(@PathVariable Long id) {
        return bookService.getById(id).map(book -> modelMapper.map(book,BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable Long id) {
        var book = bookService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("/{id}")
    public BookDTO update(@PathVariable Long id, BookDTO dto) {
        return bookService.getById(id)
                .map(book -> {
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());
                    book = bookService.update(book);
                    return modelMapper.map(book, BookDTO.class);
                }).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        var filter = modelMapper.map(dto, Book.class);
        var result = bookService.find(filter, pageRequest);
        var list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException e) {
        var bindingResult = e.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public ApiErrors handleBusinessException(BusinessException e) {
        return new ApiErrors(e);
    }

}
