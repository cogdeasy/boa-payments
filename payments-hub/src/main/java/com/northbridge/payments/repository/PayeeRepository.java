package com.northbridge.payments.repository;

import com.northbridge.payments.domain.Payee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayeeRepository extends JpaRepository<Payee, Long> {

    List<Payee> findByCifAndActiveTrueOrderByNameAsc(String cif);

    Optional<Payee> findByPayeeId(String payeeId);
}
