package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@SpringBootTest //통합테스트이므로 전부 다 메모리에 띄워두고 테스트하는 것
@AutoConfigureWebTestClient //WebTestClient 띄우기 위해
public class CustomerControllerTestForIntegrationTest {
    /**
     * @SpringBootTest를 수행하는 경우
     * */

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void 한건찾기_테스트() {
        System.out.println("===================================");
        Flux<Customer> fCustomer = customerRepository.findAll();
        fCustomer.subscribe(new Consumer<Customer>() {
            @Override
            public void accept(Customer customer) {
                System.out.println("데이터");
                System.out.println(customer);
            }
        });
    }
}
