package com.kostkin.model;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DeleteMessage implements CloudMessage {
    private String fileName;
    public DeleteMessage(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.DELETE_FILE;
    }
}
