package com.kostkin.netty;

import java.io.Closeable;

public interface AuthService extends Closeable {

    boolean getNickByLoginAndPassword(String login, String password);

    void start();

    void close();


}