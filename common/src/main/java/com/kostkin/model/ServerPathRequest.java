package com.kostkin.model;

import lombok.Getter;

@Getter
public class ServerPathRequest implements CloudMessage {

    private String folder;
    public ServerPathRequest(String folder) {
        this.folder = folder;
    }


    @Override
    public MessageType getType() {
        return MessageType.SERVER_PATH_REQUEST;
    }
}
