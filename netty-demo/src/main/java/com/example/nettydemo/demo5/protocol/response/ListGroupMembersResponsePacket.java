package com.example.nettydemo.demo5.protocol.response;

import com.example.nettydemo.demo5.protocol.Packet;
import com.example.nettydemo.demo5.session.Session;
import lombok.Data;

import java.util.List;

import static com.example.nettydemo.demo5.protocol.command.Command.LIST_GROUP_MEMBERS_RESPONSE;
@Data
public class ListGroupMembersResponsePacket extends Packet {

    private String groupId;

    private List<Session> sessionList;

    @Override
    public Byte getCommand() {

        return LIST_GROUP_MEMBERS_RESPONSE;
    }
}
