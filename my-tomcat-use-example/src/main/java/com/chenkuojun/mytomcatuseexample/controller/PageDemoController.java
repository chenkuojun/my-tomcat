package com.chenkuojun.mytomcatuseexample.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenkuojun
 */
@RestController
@RequestMapping("/test")
public class PageDemoController {

    @RequestMapping("/index")
    public String HelloWorld () {
        return "index";
    }

}
