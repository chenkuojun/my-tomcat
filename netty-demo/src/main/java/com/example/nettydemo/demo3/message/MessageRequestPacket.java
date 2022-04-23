package com.example.nettydemo.demo3.message;

import com.example.nettydemo.demo3.Packet;
import lombok.Data;
import static com.example.nettydemo.demo3.Command.MESSAGE_REQUEST;
@Data
public class MessageRequestPacket extends Packet {

    private String message;

    @Override
    public Byte getCommand() {
        return MESSAGE_REQUEST;
    }
}
