package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerService;
import com.example.fluxdemo.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest    //Repository가 떠있지 않은 상태이므로 Repository 이용한 테스트 불가
@Import(CustomerService.class)
public class CustomerControllerTest {
    /**
     * @WebFluxTest를 이용하는 경우
     * */
    @MockBean
    CustomerRepository repository;  //가짜 객체이므로 findAll() 등의 메서드 사용 불가

    @Autowired
    private WebTestClient webClient;    //비동기로 http 요청

    @Test
    public void 한건찾기_테스트() {

        //given
        Mono<Customer> givenData = Mono.just(new Customer("Jack", "Bauer"));

        //stub -> 행동 지시
        when(repository.findById(1L))
                .thenReturn(givenData);

        webClient.get().uri("/customer/{id}", 1L)
                .exchange()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Jack")
                .jsonPath("$.lastName").isEqualTo("Bauer");
    }
}
