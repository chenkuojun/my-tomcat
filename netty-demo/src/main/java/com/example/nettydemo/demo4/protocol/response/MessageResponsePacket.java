package com.example.nettydemo.demo4.protocol.response;

import com.example.nettydemo.demo4.protocol.Packet;
import lombok.Data;
import static com.example.nettydemo.demo4.protocol.command.Command.MESSAGE_RESPONSE;
@Data
public class MessageResponsePacket extends Packet {

    private String message;

    @Override
    public Byte getCommand() {

        return MESSAGE_RESPONSE;
    }
}
