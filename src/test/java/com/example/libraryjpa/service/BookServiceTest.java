package com.example.libraryjpa.service;

import com.example.libraryjpa.transfer.BookRequest;
import com.example.libraryjpa.transfer.BookResponse;
import com.example.libraryjpa.exception.BookNotFoundException;
import com.example.libraryjpa.entity.Book;
import com.example.libraryjpa.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository; // fake

    @InjectMocks
    private BookService bookService; // real, with the mock inside

    private Book cleanCode;
    private Book effectiveJava;

    @BeforeEach
    void setUp() {
        cleanCode = new Book("Clean Code", "Robert C. Martin", 2008);
        cleanCode.setId(1L);
        effectiveJava = new Book("Effective Java", "Joshua Bloch", 2018);
        effectiveJava.setId(3L);
    }

    @Test
    void findByIdReturnsTheBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(cleanCode));

        BookResponse response = bookService.findById(1L);

        assertEquals("Clean Code", response.title());
        assertEquals(2008, response.publishedYear());
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        BookNotFoundException ex = assertThrows(BookNotFoundException.class,
                () -> bookService.findById(99L));

        assertEquals("Book not found: 99", ex.getMessage());
    }

    @Test
    void findAvailableMapsToResponses() {
        when(bookRepository.findAvailableBooks()).thenReturn(List.of(cleanCode, effectiveJava));

        List<BookResponse> responses = bookService.findAvailable();

        assertEquals(2, responses.size());
        assertEquals("Clean Code", responses.get(0).title());
        assertEquals("Effective Java", responses.get(1).title());
    }

    @Test
    void createSavesTheBook() {
        BookRequest request = new BookRequest("Refactoring", "Martin Fowler", 2018);
        Book saved = new Book("Refactoring", "Martin Fowler", 2018);
        saved.setId(5L);
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        BookResponse response = bookService.create(request);

        assertEquals(5L, response.id());
        assertEquals("Refactoring", response.title());
        verify(bookRepository).save(any(Book.class)); // called once
    }

    @Test
    void updateChangesTheFields() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(cleanCode));
        // save returns the same book it receives
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BookRequest request = new BookRequest("Clean Code (2nd Edition)", "Robert C. Martin", 2025);

        BookResponse response = bookService.update(1L, request);

        assertEquals(1L, response.id());
        assertEquals("Clean Code (2nd Edition)", response.title());
        assertEquals(2025, response.publishedYear());
        verify(bookRepository).save(cleanCode);
    }

    @Test
    void updateThrowsWhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        BookRequest request = new BookRequest("Any title", "Any author", 2020);

        assertThrows(BookNotFoundException.class, () -> bookService.update(99L, request));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteRemovesTheBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.delete(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> bookService.delete(99L));

        verify(bookRepository, never()).deleteById(any());
    }

    // Extra: ArgumentCaptor shows WHAT was passed to save()
    @Test
    void createSavesAnAvailableBook() {
        BookRequest request = new BookRequest("Refactoring", "Martin Fowler", 2018);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);

        bookService.create(request);

        verify(bookRepository).save(captor.capture());
        Book savedBook = captor.getValue();
        assertEquals("Martin Fowler", savedBook.getAuthor());
        assertTrue(savedBook.isAvailable());
    }
}
