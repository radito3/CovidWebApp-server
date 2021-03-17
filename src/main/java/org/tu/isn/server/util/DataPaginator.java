package org.tu.isn.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        try (BufferedReader inputReader = Files.newBufferedReader(Paths.get(inputFileName));
            BufferedReader outputReader = Files.newBufferedReader(Paths.get(outputFileName))) {

            return Stream.concat(inputReader.lines().skip(1), outputReader.lines().skip(1)) //skip csv headers
                         .filter(filter)
                         .skip((long) page * batchLen)
                         .limit(batchLen)
                         .map(entryProducer)
                         .collect(Collectors.toList());
        }
    }

    public static class Builder {
        private int page;
        private String inputFileName;
        private String outputFileName;
        private int batchLen;
        private Predicate<String> filter = line -> true;

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
