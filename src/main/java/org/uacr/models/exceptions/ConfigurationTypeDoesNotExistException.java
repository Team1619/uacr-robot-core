package org.uacr.models.exceptions;

public class ConfigurationTypeDoesNotExistException extends ConfigurationException {

    public ConfigurationTypeDoesNotExistException(String type) {
        super("Type " + type + " does not exsist");
    }
}
