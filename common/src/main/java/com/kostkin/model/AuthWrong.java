package com.kostkin.model;

public class AuthWrong implements CloudMessage {
    @Override
    public MessageType getType() {
        return MessageType.AUTH_WRONG;
    }
}
