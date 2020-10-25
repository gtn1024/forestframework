package io.forestframework.core.http.routing;

public enum Message {
    PREHANDLER1,
    PREHANDLER2,

    HANDLER,
    HANDLER1,
    HANDLER2,

    CUSTOM_ERROR_HANDLER,
    CUSTOM_404_ERROR_HANDLER,
    CUSTOM_4XX_ERROR_HANDLER,
    CUSTOM_500_ERROR_HANDLER,

    ERROR_IN_PREHANDLER,
    ERROR_IN_POSTHANDLER,
    ERROR_IN_CUSTOM_ERROR_HANDLER
}