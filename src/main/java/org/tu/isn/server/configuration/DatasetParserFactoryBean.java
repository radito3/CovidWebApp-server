package org.tu.isn.server.configuration;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;
import org.tu.isn.server.datasets.Covid19DatasetParser;
import org.tu.isn.server.datasets.DatasetParser;

@Component("covid19dataset")
public class DatasetParserFactoryBean implements FactoryBean<DatasetParser> {

    @Override
    public DatasetParser getObject() {
        return new Covid19DatasetParser();
    }

    @Override
    public Class<?> getObjectType() {
        return DatasetParser.class;
    }
}
