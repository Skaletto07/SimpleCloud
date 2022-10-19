package com.kostkin.model;

public record AuthMessage(String login, String password) implements CloudMessage {

    @Override
    public MessageType getType() {
        return MessageType.AUTH_MESSAGE;
    }
}
