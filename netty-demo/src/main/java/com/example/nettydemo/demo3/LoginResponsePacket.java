package com.example.nettydemo.demo3;


import lombok.Data;

import static com.example.nettydemo.demo3.Command.LOGIN_RESPONSE;
@Data
public class LoginResponsePacket extends Packet {
    private boolean success;

    private String reason;


    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}
