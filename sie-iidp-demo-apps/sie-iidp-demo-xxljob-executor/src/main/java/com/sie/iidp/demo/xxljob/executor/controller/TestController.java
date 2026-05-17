package com.sie.iidp.demo.xxljob.executor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lijun10
 * @date 2023/6/18 9:03
 */
@RestController
public class TestController {

    @GetMapping(value = "/sayHello")
    public String sayHello(){
        return "hello, xxljob";
    }
}
