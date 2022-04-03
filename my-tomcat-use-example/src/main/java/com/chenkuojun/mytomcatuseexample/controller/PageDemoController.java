package com.chenkuojun.mytomcatuseexample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenkuojun
 */
@Controller
@RequestMapping("/test")
public class PageDemoController {

    //@RequestMapping("/index")
    //public String HelloWorld () {
    //    return "index";
    //}

    @RequestMapping("/aaa")
    public String fmIndex(ModelMap modelMap) {

        Map<String, String> map = new HashMap<>();

        map.put("name", "aoppp");
        map.put("desc", "描述");

        // 添加属性 给模版
        modelMap.addAttribute("user", map);

        return "aaa";
    }

}
