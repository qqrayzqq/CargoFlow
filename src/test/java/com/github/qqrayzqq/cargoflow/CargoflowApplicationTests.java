package com.github.qqrayzqq.cargoflow;

import com.github.qqrayzqq.cargoflow.elasticsearch.repository.AddressSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CargoflowApplicationTests {

    @MockitoBean
    AddressSearchRepository addressSearchRepository;

    @Test
    void contextLoads() {
    }

}
