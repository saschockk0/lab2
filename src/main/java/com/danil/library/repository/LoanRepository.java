package com.danil.library.repository;

import com.danil.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByReaderId(Long readerId);
    List<Loan> findByReturnDateIsNull();
    List<Loan> findByReturnDateIsNotNull();
    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
    List<Loan> findByReturnDateIsNullAndDueDateBefore(LocalDate date);
}
