package com.example.spring3edutest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class Spring3EduTestApplicationTests {
    @Test
    void contextLoads() {
        log.info("Just Test");
        log.info("그리고 한글 테스트");
//        Spring3EduTestApplication.main(new String[]{});
    }
}
