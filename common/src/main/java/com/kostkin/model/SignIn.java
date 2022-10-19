package com.kostkin.model;

public class SignIn implements CloudMessage {
    @Override
    public MessageType getType() {
        return MessageType.SING_IN;
    }
}
