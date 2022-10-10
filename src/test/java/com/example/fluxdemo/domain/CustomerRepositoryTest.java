package com.example.fluxdemo.domain;


import com.example.fluxdemo.DBinit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

@DataR2dbcTest
@Import(DBinit.class)
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void 한건찾기_테스트() {
        customerRepository.findById(1L).subscribe(c -> {
            System.out.println(c);
        });

        Mono<Customer> mCustomer = customerRepository.findById(2L);

        StepVerifier
                .create(customerRepository.findById(2L))
                .expectNextMatches(c -> {
                    return c.getFirstName().equals("Chloe") && c.getLastName().equals("O'Brian");
                })
                .expectComplete()
                .verify();
    }
}
