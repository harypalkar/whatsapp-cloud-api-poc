package com.whatsflow.company.repository;


import com.whatsflow.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findBySlugAndDeletedFalse(String slug);


}
