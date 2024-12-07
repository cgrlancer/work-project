package com.lancer.workflow.modules.activiti.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author i
 * @date 2024/12/6 16:54
 * @desciption:
 */
@RestController
public class TestController {

    @RequestMapping("/test")
    public String resSponse(){
        return "Hellow Activiti5.22";
    }
}
