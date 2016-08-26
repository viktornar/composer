package com.github.viktornar.controller.composer;

import com.github.viktornar.model.Atlas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Main page controller. Will be displayed by default.
 *
 * @author v.nareiko
 */
@Controller
@RequestMapping("/compose")
public class ComposeController {
    @RequestMapping(method = RequestMethod.POST)
    public String postCompose(@ModelAttribute("atlas") Atlas atlas, ModelMap model) {
        List<String> properties = new ArrayList<>();

        properties.add(atlas.getRows().toString());
        properties.add(atlas.getColumns().toString());
        properties.add(atlas.getOrientation());
        properties.add(atlas.getSize());
        properties.add(atlas.getZoom().toString());
        properties.add(atlas.getExtent().toString());

        // For testing
        model.addAttribute("allProperties", properties);

        return "compose";
    }
}
