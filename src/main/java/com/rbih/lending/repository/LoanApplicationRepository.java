package com.rbih.lending.repository;

import com.rbih.lending.entity.LoanApplicationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, UUID> {
}
