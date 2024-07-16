CREATE TABLE company (
    company_code VARCHAR(255) NOT NULL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL
);

CREATE TABLE stocks_history (
    id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    company_code VARCHAR(255) NOT NULL,
    trade_date DATE NOT NULL,
    close_price BIGINT NOT NULL,
    FOREIGN KEY (company_code) REFERENCES company(company_code)
);
