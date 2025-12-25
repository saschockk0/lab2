package com.rus.bank.service;

import com.rus.bank.dto.CustomerDto;
import com.rus.bank.dto.CreateCustomerRequest;
import com.rus.bank.dto.UpdateCustomerRequest;
import com.rus.bank.exception.NotFoundException;
import com.rus.bank.model.Customer;
import com.rus.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDto create(CreateCustomerRequest req) {
        log.info("Creating customer with taxId: {}", req.getTaxId());
        
        if (customerRepository.existsByTaxId(req.getTaxId())) {
            throw new IllegalStateException("Customer with taxId " + req.getTaxId() + " already exists");
        }
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Customer with email " + req.getEmail() + " already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(req.getFirstName());
        customer.setLastName(req.getLastName());
        customer.setTaxId(req.getTaxId());
        customer.setEmail(req.getEmail());
        customer.setPhone(req.getPhone());
        customer.setAddress(req.getAddress());

        customer = customerRepository.save(customer);
        log.info("Created customer with id: {}", customer.getId());
        return toDto(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> getPage(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public CustomerDto getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer with id=" + id + " not found"));
        return toDto(customer);
    }

    @Transactional
    public CustomerDto update(Long id, UpdateCustomerRequest req) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer with id=" + id + " not found"));

        if (req.getFirstName() != null) customer.setFirstName(req.getFirstName());
        if (req.getLastName() != null) customer.setLastName(req.getLastName());
        if (req.getEmail() != null) {
            if (customerRepository.existsByEmail(req.getEmail()) && !customer.getEmail().equals(req.getEmail())) {
                throw new IllegalStateException("Email " + req.getEmail() + " already exists");
            }
            customer.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) customer.setPhone(req.getPhone());
        if (req.getAddress() != null) customer.setAddress(req.getAddress());

        customer = customerRepository.save(customer);
        log.info("Updated customer with id: {}", customer.getId());
        return toDto(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer with id=" + id + " not found"));
        customerRepository.delete(customer);
        log.info("Deleted customer with id: {}", id);
    }

    private CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setTaxId(customer.getTaxId());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }
}

