package com.github.viktornar.controller.composer;

import com.github.viktornar.model.Atlas;
import com.github.viktornar.model.Extent;
import com.github.viktornar.task.PrintTask;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import static java.lang.String.format;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import static com.github.viktornar.utils.Helper.*;

/**
 * Main page controller. Will be displayed by default.
 *
 * @author v.nareiko
 */
@Controller
@RequestMapping("/")
public class ComposeController {
    private ExecutorService executorService;
    @Value("${atlas.folder}")
    private String atlasFolder;
    @Value("${atlas.name.prefix}")
    private String atlasNamePrefix;

    @RequestMapping(value="/compose", method = RequestMethod.POST)
    public String postCompose(@ModelAttribute("atlas") Atlas atlas, final RedirectAttributes redirectAttributes) {
        String id = getRandomlyNames(8, 1)[0];

        atlas.setAtlasFolder(format("%s/%s", atlasFolder, id));
        atlas.setAtlasName(atlasNamePrefix + id);

        final int POOL_SIZE = atlas.getColumns() * atlas.getRows() + 2;
        ExecutorService executorService = getExecutorService(POOL_SIZE);
        runPrintTask(executorService, atlas);

        // TODO: Save atlas model to database

        redirectAttributes
                .addAttribute("id", id);

        return "redirect:/status/{id}?timeout=30";
    }

    @RequestMapping(value="/status/{id}", method = RequestMethod.GET)
    public String postCompose(@PathVariable("id") String id,
                              ModelMap model,
                              @RequestParam(value = "timeout", required = true) int timeout) {

        // TODO: get atlas properties from database

        if(timeout > 0){
            timeout -= 1;
            model.addAttribute("errorExist", false);
        }else{
            model.addAttribute("errorExist", true);
        }

        model.addAttribute("timeout", timeout);
        model.addAttribute("fileExist", isFileExist(id, atlasFolder, atlasNamePrefix));
        model.addAttribute("atlasId", id);

        return "status";
    }

    @RequestMapping(value = "/print", method = RequestMethod.GET)
    public String displayById(
            @RequestParam(value = "orientation", required = true) String orientation,
            @RequestParam(value = "size", required = true) String size,
            @RequestParam(value = "xmin", required = true) Double xmin,
            @RequestParam(value = "ymin", required = true) Double ymin,
            @RequestParam(value = "xmax", required = true) Double xmax,
            @RequestParam(value = "ymax", required = true) Double ymax,
            ModelMap model
    ) {

        Extent extent = new Extent();
        extent.setXmin(xmin);
        extent.setYmin(ymin);
        extent.setXmax(xmax);
        extent.setYmax(ymax);

        Atlas atlas = new Atlas();
        atlas.setExtent(extent);
        atlas.setOrientation(orientation);
        atlas.setSize(size);

        model.addAttribute("atlas", atlas);
        return "map";
    }

    /**
     *
     * @param id
     * @param response
     */
    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public void getFile(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            String _atlasFolder = format("%s/%s", atlasFolder, id);
            String _atlasName = format("%s%s.pdf", atlasNamePrefix, id);

            String filePathToBeServed = format("%s/%s", _atlasFolder, _atlasName);
            File fileToDownload = new File(filePathToBeServed);
            InputStream inputStream = new FileInputStream(fileToDownload);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", format("attachment; filename=%s", _atlasName));
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    private void runPrintTask(ExecutorService executorService, Atlas atlas) {
        executorService.submit(new PrintTask(executorService, atlas));
    }
}
