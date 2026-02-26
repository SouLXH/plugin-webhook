package com.wxy97.webhook.bean.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 微信企业号文本消息
 * Author: wxy97.com
 * Date: 2026-02-26
 * Description: 微信消息格式适配
 */
@Data
@NoArgsConstructor
public class WeChatTextMessage {
    private String msgtype = "text";
    
    @JsonProperty("text")
    private TextContent text;

    @Data
    @NoArgsConstructor
    public static class TextContent {
        private String content;
        
        @JsonProperty("mentioned_list")
        private List<String> mentionedList;
        
        @JsonProperty("mentioned_mobile_list")
        private List<String> mentionedMobileList;

        public TextContent(String content) {
            this.content = content;
        }

        public TextContent(String content, List<String> mentionedList, List<String> mentionedMobileList) {
            this.content = content;
            this.mentionedList = mentionedList;
            this.mentionedMobileList = mentionedMobileList;
        }
    }

    public WeChatTextMessage(String content) {
        this.text = new TextContent(content);
    }

    public WeChatTextMessage(String content, List<String> mentionedList, List<String> mentionedMobileList) {
        this.text = new TextContent(content, mentionedList, mentionedMobileList);
    }
}