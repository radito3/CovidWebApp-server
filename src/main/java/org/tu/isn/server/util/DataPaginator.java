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

public class DataPaginator {
    private final int page;
    private final String inputFileName;
    private final String outputFileName;
    private final int batchLen;
    private final long inputLimit;
    private final long outputLimit;

    private DataPaginator(Builder builder) {
        page = builder.page;
        inputFileName = builder.inputFileName;
        outputFileName = builder.outputFileName;
        batchLen = builder.batchLen;
        inputLimit = builder.inputLimit;
        outputLimit = builder.outputLimit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> List<T> getPageOfResources(Function<String, ? extends T> entryProducer) throws IOException {
        List<T> result = new ArrayList<>();
        long offsetFrom = page == 0 ? 0 : (long) page * batchLen;
        long offsetTo = page == 0 ? batchLen : (long) (page + 1) * batchLen;
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

    public static void consumeFileLines(String fileName, long offsetFrom, long offsetTo, Consumer<String> consumer) throws IOException {
        Path outputDataFile = Paths.get(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(outputDataFile), StandardCharsets.UTF_8))) {
            reader.readLine(); //skip csv headers
            reader.lines()
                  .skip(offsetFrom)
                  .limit(offsetTo - offsetFrom)
                  .forEach(consumer);
        }
    }

    public static class Builder {
        private int page;
        private String inputFileName;
        private String outputFileName;
        private int batchLen;
        private long inputLimit;
        private long outputLimit;

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

        public Builder setInputLimit(long inputLimit) {
            this.inputLimit = inputLimit;
            return this;
        }

        public Builder setOutputLimit(long outputLimit) {
            this.outputLimit = outputLimit;
            return this;
        }

        public DataPaginator build() {
            return new DataPaginator(this);
        }
    }

}
