package com.project.code.Repo;

import com.project.code.Model.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Customer findByid(@Param("id") Long id);
}
