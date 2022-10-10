package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerRepository;
import com.example.fluxdemo.domain.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final Sinks.Many<Customer> sink = Sinks.many().multicast().onBackpressureBuffer();
    // sink는 모든 클라이언트가 접근할 수 있음.

    /**
     * Sinks란??
     * A요청 -> Flux -> Stream
     * B요청 -> Flux -> Stream
     * A,B 각 요청에 대한 응답이 Flux라 할 때 두 개의 응답 Stream을 Merge할 수 있음
     * Flux.merge -> sink (싱크가 맞춰짐)
     *
     * Sinks.many().multicast() : 새로 생긴 데이터만 구독자에게 전달해주는 방식
     * */

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
        // just는 파라미터로 받은 데이터를 순차적으로 꺼내서 던져주는 역할
        // 1초에 하나씩 데이터가 반환되나 웹 페이지에는 모든 데이터가 다 응답된 후에 한 번에 뜸
    }

    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> fluxstream() {
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
        // produces 타입을 MediaType.APPLICATION_STREAM_JSON_VALUE로 지정해서 데이터가 반환될 때마다 바로 웹 페이제에 표시되도록 함
    }

    @GetMapping(value = "/customer", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Customer> findAll() {
        return customerRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
        //하나씩 응답된 후에 클라이언트가 더이상 기다리지 않음
    }

    //리턴되는 값이 하나이면 Mono를 통해 리턴하고, 여러 개면 Flux를 통해 리턴
    @GetMapping("/customer/{id}")
    public Mono<Customer> findById(@PathVariable Long id) {
        return customerRepository.findById(id).log();
    }

    // ServerSentEvent가 있으면 produces는 자동으로 MediaType.TEXT_EVENT_STREAM_VALUE 가 되어 생략 가능
    @GetMapping(value = "/customer/sse")
    public Flux<ServerSentEvent<Customer>> findAllSSE() {
        // 싱크의 데이터가 합쳐지면 그 합쳐진 데이터를 응답
        return sink
                .asFlux()
                .map(customer ->
                        ServerSentEvent
                                .builder(customer)
                                .build())
                .doOnCancel(() -> {
                            sink.asFlux().blockLast();  //blockLast로 취소되었을 때 마지막 데이터임을 알려줌
                        }
                );
        // doOnCancel에 blockLast를 안해주면 클라이언트가 연결을 끊은 후에 다시 /customer/sse로 연결하려고 해도 정상동작하지 않음
    }

    @PostMapping("/customer")
    public Mono<Customer> save(){
        return customerRepository
                .save(new Customer("Gildong", "Hong"))
                .doOnNext(customer -> {
                    sink.tryEmitNext(customer);     //sink에 새 데이터를 추가
                });
        //save 된 후 어떻게 consume할 지도 구현해야 sink가 새로 저장된 데이터를 인지 가능
    }
}
