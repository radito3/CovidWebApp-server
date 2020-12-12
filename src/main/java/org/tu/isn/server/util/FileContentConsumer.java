package org.tu.isn.server.util;

import java.io.BufferedReader;
import java.io.IOException;

@FunctionalInterface
public interface FileContentConsumer {

    void accept(BufferedReader reader) throws IOException;
}