package com.kostkin.netty.serial;

import com.kostkin.model.*;
import com.kostkin.netty.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private final AuthService authService;
    private Path serverDir;
    public FileHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
        ctx.writeAndFlush(new SignIn());
//        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverDir.resolve(fileRequest.fileName())));
        } else if (cloudMessage instanceof ServerPathRequest serverPathRequest) {
            Path normalize = Path.of(serverDir.toString(), serverPathRequest.getFolder()).normalize();
            if (Files.isDirectory(normalize)) {
            ctx.writeAndFlush(new ListMessage(serverDir.resolve(serverPathRequest.getFolder())));
            }
            serverDir = normalize;
        } else if (cloudMessage instanceof DeleteMessage message) {
            String fileName = message.getFileName();
            Path file = Path.of(serverDir.toString(),fileName);
            Files.delete(file);
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof RenameFile renameFile) {
            String oldFileName = renameFile.getOldFileName();
            String newFileName = renameFile.getNewFileName();
            Path source = Path.of(serverDir.toString(), oldFileName);
            Files.move(source, source.resolveSibling(newFileName));
            ctx.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof AuthMessage authMessage) {
            if (authService.getNickByLoginAndPassword(authMessage.login(),authMessage.password())) {
                ctx.writeAndFlush(new ListMessage(serverDir));
            } else ctx.writeAndFlush(new AuthWrong());

        }
    }

}