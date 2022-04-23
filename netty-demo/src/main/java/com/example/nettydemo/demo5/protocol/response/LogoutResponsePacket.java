package com.example.nettydemo.demo5.protocol.response;

import com.example.nettydemo.demo5.protocol.Packet;
import lombok.Data;
import static com.example.nettydemo.demo5.protocol.command.Command.*;

@Data
public class LogoutResponsePacket extends Packet {

    private boolean success;

    private String reason;


    @Override
    public Byte getCommand() {

        return LOGOUT_RESPONSE;
    }
}
