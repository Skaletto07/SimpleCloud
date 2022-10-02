package com.kostkin.simpleCloudClient;

public record Network<I, O>(I inputStream, O outputStream) {
}