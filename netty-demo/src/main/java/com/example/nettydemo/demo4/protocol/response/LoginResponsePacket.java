package com.example.nettydemo.demo4.protocol.response;

import com.example.nettydemo.demo4.protocol.Packet;
import lombok.Data;

import static com.example.nettydemo.demo4.protocol.command.Command.LOGIN_RESPONSE;
@Data
public class LoginResponsePacket extends Packet {
    private boolean success;

    private String reason;


    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}
