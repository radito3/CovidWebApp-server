package org.tu.isn.server.util;

import java.io.BufferedReader;
import java.io.IOException;

@FunctionalInterface
public interface FileContentProcessor<T> {

    T accept(BufferedReader reader) throws IOException;
}
