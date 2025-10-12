package com.danil.library.controller;

import com.danil.library.dto.BookDto;
import com.danil.library.dto.CreateBookRequest;
import com.danil.library.dto.UpdateBookRequest;
import com.danil.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BookDto> create(@Valid @RequestBody CreateBookRequest req) {
        BookDto saved = service.create(req);
        return ResponseEntity.created(URI.create("/api/books/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<BookDto> getAll() { return service.getAll(); }

    @GetMapping("/page")
    public Page<BookDto> page(Pageable pageable) { return service.getPage(pageable); }

    @GetMapping("/{id}")
    public BookDto get(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/author/{authorId}")
    public List<BookDto> byAuthor(@PathVariable Long authorId) { return service.findByAuthor(authorId); }

    @GetMapping("/search")
    public List<BookDto> search(@RequestParam("q") String q) { return service.searchByTitle(q); }

    @GetMapping("/available")
    public List<BookDto> available() { return service.listAvailable(); }

    @PatchMapping("/{id}/borrow")
    public BookDto borrow(@PathVariable Long id) { return service.borrow(id); }

    @PatchMapping("/{id}/return")
    public BookDto giveBack(@PathVariable Long id) { return service.giveBack(id); }
}
