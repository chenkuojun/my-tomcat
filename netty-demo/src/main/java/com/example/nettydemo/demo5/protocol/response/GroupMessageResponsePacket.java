package com.example.nettydemo.demo5.protocol.response;

import com.example.nettydemo.demo5.protocol.Packet;
import com.example.nettydemo.demo5.session.Session;
import lombok.Data;

import static com.example.nettydemo.demo5.protocol.command.Command.GROUP_MESSAGE_RESPONSE;
@Data
public class GroupMessageResponsePacket extends Packet {

    private String fromGroupId;

    private Session fromUser;

    private String message;

    @Override
    public Byte getCommand() {

        return GROUP_MESSAGE_RESPONSE;
    }
}
