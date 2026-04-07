package com.github.qqrayzqq.cargoflow_practice;

import com.github.qqrayzqq.cargoflow_practice.elasticsearch.repository.AddressSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CargoflowPracticeApplicationTests {

    @MockitoBean
    AddressSearchRepository addressSearchRepository;

    @Test
    void contextLoads() {
    }

}
