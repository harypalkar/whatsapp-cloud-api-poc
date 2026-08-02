package com.whatsflow.customer.service;


import com.whatsflow.common.api.PageResponse;
import com.whatsflow.customer.domain.Customer;
import com.whatsflow.customer.dto.CustomerRequest;
import com.whatsflow.customer.dto.CustomerResponse;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository repo;
    public CustomerService(CustomerRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(String q, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Customer> page = (q == null || q.isBlank())
                ? repo.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : repo.findByTenantIdAndDeletedFalseAndNameContainingIgnoreCaseOrTenantIdAndDeletedFalseAndMobileE164Containing(
                        tenantId, q, tenantId, q, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Customer c = new Customer();
        c.setTenantId(tenantId);
        c.setMobileE164(req.mobileE164());
        c.setName(req.name());
        c.setEmail(req.email());
        c.setOptedIn(req.optedIn() == null || req.optedIn());
        return toDto(repo.save(c));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        return toDto(require(id));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest req) {
        Customer c = require(id);
        c.setName(req.name());
        c.setEmail(req.email());
        if (req.optedIn() != null) c.setOptedIn(req.optedIn());
        return toDto(repo.save(c));
    }

    @Transactional
    public void softDelete(UUID id) {
        Customer c = require(id);
        c.setDeleted(true);
        repo.save(c);
    }

    @Transactional
    public CustomerResponse optIn(UUID id, boolean optedIn) {
        Customer c = require(id);
        c.setOptedIn(optedIn);
        return toDto(repo.save(c));
    }

    @Transactional
    public CustomerResponse blacklist(UUID id, boolean blacklisted) {
        Customer c = require(id);
        c.setBlacklisted(blacklisted);
        return toDto(repo.save(c));
    }

    private Customer require(UUID id) {
        return repo.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private CustomerResponse toDto(Customer c) {
        return new CustomerResponse(c.getId(), c.getMobileE164(), c.getName(), c.getEmail(), c.isOptedIn(), c.isBlacklisted());
    }
}
