package com.example.nettydemo.demo5.protocol.response;

import com.example.nettydemo.demo5.protocol.Packet;
import lombok.Data;

import static com.example.nettydemo.demo5.protocol.command.Command.LOGIN_RESPONSE;
@Data
public class LoginResponsePacket extends Packet {
    private String userId;

    private String userName;

    private boolean success;

    private String reason;


    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}
