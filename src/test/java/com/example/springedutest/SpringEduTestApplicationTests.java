package com.example.springedutest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class SpringEduTestApplicationTests {
    @Test
    void contextLoads() {
        log.info("Just Test");
        log.info("그리고 한글 테스트");
//        SpringEduTestApplication.main(new String[]{});
    }
}
