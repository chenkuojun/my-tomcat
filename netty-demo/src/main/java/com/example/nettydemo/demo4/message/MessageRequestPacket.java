package com.example.nettydemo.demo4.message;

import com.example.nettydemo.demo4.protocol.Packet;
import lombok.Data;

import static com.example.nettydemo.demo4.protocol.command.Command.MESSAGE_REQUEST;

@Data
public class MessageRequestPacket extends Packet {

    private String message;

    public MessageRequestPacket(String message) {
        this.message = message;
    }

    @Override
    public Byte getCommand() {
        return MESSAGE_REQUEST;
    }
}
