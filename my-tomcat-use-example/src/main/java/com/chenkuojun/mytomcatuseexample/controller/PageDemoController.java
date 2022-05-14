package com.chenkuojun.mytomcatuseexample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenkuojun
 */
@Controller
@RequestMapping("/test")
@Slf4j
public class PageDemoController {

    @RequestMapping("/bbb")
    public void HelloWorld (HttpServletResponse response) throws IOException {
        response.sendRedirect("/test/aaa");
    }

    @RequestMapping("/ccc")
    public String HelloWorld () throws IOException {
        return null;
    }

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
