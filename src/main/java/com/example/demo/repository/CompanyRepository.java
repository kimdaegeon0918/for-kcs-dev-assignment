package com.example.demo.repository;

import com.example.demo.entity.Company;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Company findByCompanyCode(String companyCode);
}
