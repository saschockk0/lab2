package com.danil.library.controller;

import com.danil.library.dto.AuthorDto;
import com.danil.library.dto.CreateAuthorRequest;
import com.danil.library.dto.UpdateAuthorRequest;
import com.danil.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService service;

    public AuthorController(AuthorService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AuthorDto> create(@Valid @RequestBody CreateAuthorRequest req) {
        AuthorDto saved = service.create(req);
        return ResponseEntity.created(URI.create("/api/authors/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<AuthorDto> getAll() { return service.getAll(); }

    @GetMapping("/page")
    public Page<AuthorDto> page(Pageable pageable) { return service.getPage(pageable); }

    @GetMapping("/{id}")
    public AuthorDto get(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public AuthorDto update(@PathVariable Long id, @Valid @RequestBody UpdateAuthorRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
