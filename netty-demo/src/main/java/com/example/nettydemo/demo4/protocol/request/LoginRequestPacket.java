package com.example.nettydemo.demo4.protocol.request;

import com.example.nettydemo.demo4.protocol.Packet;
import lombok.Data;

import static com.example.nettydemo.demo4.protocol.command.Command.LOGIN_REQUEST;

@Data
public class LoginRequestPacket extends Packet {
    private String userId;

    private String username;

    private String password;

    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}
