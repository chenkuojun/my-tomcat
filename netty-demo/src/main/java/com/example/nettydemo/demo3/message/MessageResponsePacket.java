package com.example.nettydemo.demo3.message;

import com.example.nettydemo.demo3.Packet;
import lombok.Data;
import static com.example.nettydemo.demo3.Command.MESSAGE_RESPONSE;
@Data
public class MessageResponsePacket extends Packet {

    private String message;

    @Override
    public Byte getCommand() {

        return MESSAGE_RESPONSE;
    }
}
