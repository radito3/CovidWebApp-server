package org.tu.isn.server.datasets;

import java.io.IOException;
import java.io.InputStream;

public interface DatasetParser {

    int getDateIndex();

    int getCountryNameIndex();

    int getDeathsIndex();

    int getRecoveredIndex();

    int getActiveIndex();

    InputStream getContent() throws IOException;

}
