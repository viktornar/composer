/*
 This file is part of Composer.
 Composer is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
 Copyright 2016 (C) Viktor Nareiko
 */
package com.github.viktornar.task;

import com.github.viktornar.model.Atlas;
import com.github.viktornar.model.Extent;
import com.github.viktornar.service.SettingsService;
import com.github.viktornar.service.repository.Repository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static com.github.viktornar.utils.Helper.*;

import static java.lang.String.format;

public class PrintTask implements Callable<String> {
    private Logger logger = LoggerFactory.getLogger(PrintTask.class);

    private String port;
    private String hostname;
    private String contextPath;

    private Atlas atlas;
    private Repository repository;
    private ExecutorService executorService;

    private static final String DEFAULT_PORT = "9000";
    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final String DEFAULT_CONTEXT_PATH = "composer";
    private static final String DEFAULT_PRINT_CMD = "wkhtmltopdf " +
            "-s %s " +
            "-O %s " +
            "--no-stop-slow-scripts " +
            "--javascript-delay 7000 " +
            "\"http://%s:%s%s/print?xmin=%f&ymin=%f&xmax=%f&ymax=%f&size=%s&orientation=%s\" " +
            "\"%s/%s-%s-%s.pdf\"";

    public PrintTask(ExecutorService _executorService,
                     Atlas _atlas,
                     Repository _repository,
                     SettingsService settingsService
    ) {
        executorService = _executorService;
        atlas = _atlas;
        repository = _repository;

        port = settingsService.getPort();
        contextPath = settingsService.getContextPath();
        hostname = settingsService.getHostname();
    }

    @Override
    public String call() throws Exception {
        assert atlas != null;
        assert executorService != null;
        executeCommand(atlas);
        executorService.shutdown();
        return format("%s/%s.pdf", atlas.getAtlasFolder(), atlas.getAtlasName());
    }

    private String getCommand(Atlas atlas, int row, int column) {
        String _hostname = DEFAULT_HOSTNAME;
        String _contextPath = DEFAULT_CONTEXT_PATH;
        String _port = DEFAULT_PORT;

        if (hostname != null) {
            _hostname = hostname;
        }

        if (contextPath != null) {
            _contextPath = contextPath;
        }

        if (port != null) {
            _port = port;
        }

        return String.format(Locale.US,
                DEFAULT_PRINT_CMD,
                StringUtils.capitalize(atlas.getSize()),
                StringUtils.capitalize(atlas.getOrientation()),
                _hostname,
                _port,
                _contextPath,
                atlas.getExtent().getXmin(),
                atlas.getExtent().getYmin(),
                atlas.getExtent().getXmax(),
                atlas.getExtent().getYmax(),
                atlas.getSize(),
                atlas.getOrientation(),
                atlas.getAtlasFolder(),
                atlas.getAtlasName(),
                row,
                column
        );
    }

    private void executeCommand(Atlas atlas) {
        createAtlasFolder(atlas.getAtlasFolder());

        Collection<Future<?>> futures = new LinkedList<>();
        IntStream.range(1, atlas.getRows() + 1).forEachOrdered(row -> {
            IntStream.range(1, atlas.getColumns() + 1).forEachOrdered(column -> {
                futures.add(executorService.submit(() -> {
                    final Process process;
                    try {
                        logger.info(format(" [x] Start printing job [row:'%s', column: '%s'] : '%s'", row, column, atlas.toString()));
                        Extent pageExtent = getExtentOfPage(atlas, column, row);
                        Atlas atlasPage = new Atlas();
                        atlasPage.copyBean(atlas);
                        atlasPage.setExtent(pageExtent);
                        logger.info(format(" [x] Printing job command: '%s'", getCommand(atlasPage, row, column)));
                        process = Runtime.getRuntime().exec(getCommand(atlasPage, row, column));
                        process.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            });
        });

        futures.forEach(future -> {
            try {
                future.get();
                Atlas _atlas = repository.getAtlasById(atlas.getId());
                _atlas.setProgress(_atlas.getProgress() + 1);
                repository.updateAtlas(_atlas);
                logger.info(format(" [x] Finished printing job: '%s'", atlas.toString()));
                Thread.sleep(1000); // Sleep thread for 1 s for printing progress successful update
            } catch (InterruptedException | ExecutionException e) {
                logger.error(format(" [x] Error on printing job: '%s'", atlas.toString()));
                throw new RuntimeException(e);
            }
        });

        mergePages(atlas.getAtlasFolder(), format("%s.pdf", atlas.getAtlasName()));
    }
}
