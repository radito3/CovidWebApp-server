package org.tu.isn.server.datasets;

import java.io.IOException;
import java.io.InputStream;

public interface DatasetParser {

    int getDateIndex();

    int getCountryNameIndex();

    int getCountryCoordinateLatIndex();

    int getCountryCoordinateLonIndex();

    int getDeathsIndex();

    int getRecoveredIndex();

    int getActiveIndex();

    int getNumberOfFields();

    InputStream getContent() throws IOException;

}
