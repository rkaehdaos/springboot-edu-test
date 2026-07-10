package com.example.springedutest.greeting;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@Slf4j
public class GreetingController {
    public static final String template = "Hello, %s!";
    public static final AtomicLong counter = new AtomicLong();

    @GetMapping("greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        log.info("input_name:"+name);
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
