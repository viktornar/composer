package com.github.viktornar.task;

import com.github.viktornar.model.Atlas;
import com.github.viktornar.model.Extent;
import static com.github.viktornar.utils.Helper.createAtlasFolder;
import static com.github.viktornar.utils.Helper.mergePages;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static com.github.viktornar.utils.Helper.getExtentOfPage;

public class PrintTask implements Callable<String> {
    @Value("${atlas.command}")
    private String printCommand;
    private Atlas atlas;
    private ExecutorService executorService;
    private static final String DEFAULT_PRINT_CMD = "wkhtmltopdf " +
            "-s %s " +
            "-O %s" +
            " --no-stop-slow-scripts " +
            "--javascript-delay 7000 " +
            "http://localhost:9000/composer/print?xmin=%f&ymin=%f&xmax=%f&ymax=%f&size=%s&orientation=%s %s/%s-%s-%s.pdf";

    public PrintTask(ExecutorService executorService, Atlas atlas) {
        this.executorService = executorService;
        this.atlas = atlas;
    }

    @Override
    public String call() throws Exception {
        assert atlas != null;
        assert executorService != null;
        executeCommand(atlas);
        executorService.shutdown();
        return atlas.getAtlasFolder() + "/" + atlas.getAtlasName() + ".pdf";
    }

    private String getCommand(Atlas atlas, int row, int column) {
        String _printCommand = DEFAULT_PRINT_CMD;

        if (printCommand != null) {
            _printCommand = printCommand;
        }

        return String.format(Locale.US,
                _printCommand,
                StringUtils.capitalize(atlas.getSize()),
                StringUtils.capitalize(atlas.getOrientation()),
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
                        // TODO: update printing progress
                        System.out.println(" [x] Start printing job [row:'" + row + "' , column:'" + column + "'] : '" + atlas.toString() + "'");
                        Extent pageExtent = getExtentOfPage(atlas, column, row);
                        Atlas atlasPage = new Atlas();
                        atlasPage.copyBean(atlas);
                        atlasPage.setExtent(pageExtent);
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
                System.out.println(" [x] Finished printing job: '" + atlas.toString() + "'");
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(" [x] Error on printing job: '" + atlas.toString() + "'");
                throw new RuntimeException(e);
            }
        });

        mergePages(atlas.getAtlasFolder(), atlas.getAtlasName() + ".pdf");
    }
}
