package org.tu.isn.server.datasets;

import java.io.IOException;
import java.io.InputStream;

public interface DatasetParser {

    int getDateIndex();

    int getCountryNameIndex();

    int getConfirmedIndex();

    int getDeathsIndex();

    int getRecoveredIndex();

    int getActiveIndex();

    int getNewCasesIndex();

    int getNewDeathsIndex();

    int getNewRecoveredIndex();

    int getNumberOfFields();

    InputStream getContent() throws IOException;

}
