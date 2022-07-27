package com.octv.im.entity;

import lombok.Data;

import java.util.Map;

@Data
public class ChatHistoryBean {
    /** 消息id */
    private String id;
    /** 消息发送类型 */
    private Integer code;
    /** 发送人用户id */
    private String sendUserId;
    /** 发送人用户名 */
    private String username;

    private String userID;
    /** 接收人用户id，多个逗号分隔 */
    private String receiverUserId;
    /** 发送时间 */
    private String sendTime;
    /** 消息类型 */
    private Integer type;
    /** 消息内容 */
    private String msg;
    /** 消息扩展内容 */
    private Map<String, Object> ext;
    /** 消息是否已读 */
    private Boolean read;

    private String groupID;
}
