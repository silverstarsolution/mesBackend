package com.mes.mesBackend.exception;

import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.MongoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;

public class NotFoundException extends Exception {
    private Logger logger = LoggerFactory.getLogger(NotFoundException.class);
    private CustomLogger cLogger;

    public NotFoundException(String message) {
        super(message);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.error(message);
    }
}
