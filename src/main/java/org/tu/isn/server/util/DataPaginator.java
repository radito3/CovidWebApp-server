package org.tu.isn.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataPaginator {
    private final int page;
    private final String inputFileName;
    private final String outputFileName;
    private final int batchLen;
    private final Predicate<String> filter;

    private DataPaginator(Builder builder) {
        page = builder.page;
        inputFileName = builder.inputFileName;
        outputFileName = builder.outputFileName;
        batchLen = builder.batchLen;
        filter = builder.filter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> List<T> getPageOfResources(Function<String, ? extends T> entryProducer) throws IOException {
        List<T> result = new ArrayList<>();
        long offsetFrom = page == 0 ? 0 : (long) page * batchLen;
        long offsetTo = page == 0 ? batchLen : (long) (page + 1) * batchLen;
        long inputLimit = countLines(inputFileName);
        long outputLimit = countLines(outputFileName);

        if (batchLen > inputLimit + outputLimit) {
            offsetTo = inputLimit + outputLimit;
        }

        if (offsetFrom < inputLimit) { //start is within existing data
            if (offsetTo > inputLimit) { //time slice is within both files' data
                long additional = offsetTo - inputLimit;
                consumeFileLines(inputFileName, offsetFrom, inputLimit, line -> result.add(entryProducer.apply(line)));
                consumeFileLines(outputFileName, 0, additional, line -> result.add(entryProducer.apply(line)));
            } else { //time slice is within existing data only
                consumeFileLines(inputFileName, offsetFrom, offsetTo, line -> result.add(entryProducer.apply(line)));
            }
        } else { //time slice is within predicted data only
            consumeFileLines(outputFileName, offsetFrom, offsetTo, line -> result.add(entryProducer.apply(line)));
        }
        return result;
    }

    private void consumeFileLines(String fileName, long offsetFrom, long offsetTo, Consumer<String> consumer) throws IOException {
        Path file = Paths.get(fileName);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.readLine(); //skip csv headers
            reader.lines()
                  .filter(filter)
                  .skip(offsetFrom)
                  .limit(offsetTo - offsetFrom)
                  .forEach(consumer);
        }
    }

    private long countLines(String fileName) throws IOException {
        Path file = Paths.get(fileName);
        return Files.lines(file)
                    .skip(1)
//                    .filter(filter)
                    .count();
    }

    public static class Builder {
        private int page;
        private String inputFileName;
        private String outputFileName;
        private int batchLen;
        private Predicate<String> filter;

        public Builder() {
            filter = o -> true;
        }

        public Builder setPage(int page) {
            this.page = page;
            return this;
        }

        public Builder setInputFileName(String inputFileName) {
            this.inputFileName = inputFileName;
            return this;
        }

        public Builder setOutputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
            return this;
        }

        public Builder setBatchLen(int batchLen) {
            this.batchLen = batchLen;
            return this;
        }

        public Builder setFilter(Predicate<String> filter) {
            this.filter = filter;
            return this;
        }

        public DataPaginator build() {
            return new DataPaginator(this);
        }
    }

}
