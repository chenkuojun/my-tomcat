package com.example.nettydemo.demo5.protocol.request;

import com.example.nettydemo.demo5.protocol.Packet;
import lombok.Data;

import static com.example.nettydemo.demo5.protocol.command.Command.LOGIN_REQUEST;

@Data
public class LoginRequestPacket extends Packet {
    private String userName;

    private String password;

    @Override
    public Byte getCommand() {

        return LOGIN_REQUEST;
    }
}
