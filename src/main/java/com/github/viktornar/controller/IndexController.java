package com.github.viktornar.controller;

import com.github.viktornar.model.Atlas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Main page controller. Will be displayed by default.
 *
 * @author v.nareiko
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @RequestMapping(method = RequestMethod.GET)
    public String getIndex(ModelMap model) {
        model.addAttribute("atlas", new Atlas());
        return "index";
    }
}
