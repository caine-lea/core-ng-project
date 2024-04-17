package core.framework.internal.web.service;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class ErrorResponse {
    @Property(name = "id")
    public String id;

    @Property(name = "errorCode")
    public String errorCode;

    @Property(name = "message")
    public String message;
}
