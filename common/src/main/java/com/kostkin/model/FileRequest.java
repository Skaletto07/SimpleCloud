package com.kostkin.model;


public record FileRequest(String fileName) implements CloudMessage {

    @Override
    public MessageType getType() {
        return MessageType.FILE_REQUEST;
    }
}