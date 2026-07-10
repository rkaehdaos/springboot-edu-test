package com.example.springedutest.greeting;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {GreetingController.class})
@DisplayName("Greeting MockTest")
@Slf4j
class GreetingControllerMockMvcTest {

    @Autowired
    protected MockMvcTester mockMvc;

    @Test
    void greetingTest1() {
        log.debug("test1");
        assertThat(mockMvc.get().uri("/greeting"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id -> assertThat(id).asNumber().isNotNull())
                .extractingPath("$.content").isEqualTo("Hello, World!");
    }

    @Test
    void greetingTest2() {
        log.debug("test2");
        assertThat(mockMvc.get().uri("/greeting").param("name", "test2"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id -> assertThat(id).asNumber().isNotNull())
                .extractingPath("$.content").isEqualTo("Hello, test2!");
    }
}
