package com.kostkin.model;

import lombok.Getter;

@Getter
public class RenameFile implements CloudMessage {
    private String oldFileName;
    private String newFileName;

    public RenameFile(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.RENAME_FILE;
    }
}
