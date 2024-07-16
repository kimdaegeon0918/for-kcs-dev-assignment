package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void testFindByCompanyCode() {
        // given
        Company company = new Company("AAPL", "Apple Inc.");
        companyRepository.save(company);

        // when
        Company foundCompany = companyRepository.findByCompanyCode("AAPL");

        // then
        assertThat(foundCompany).isNotNull();
        assertThat(foundCompany.getCompanyCode()).isEqualTo("AAPL");
        assertThat(foundCompany.getCompanyName()).isEqualTo("Apple Inc.");
    }
}
