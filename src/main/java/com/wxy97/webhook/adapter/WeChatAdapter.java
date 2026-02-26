package com.wxy97.webhook.adapter;

import com.wxy97.webhook.bean.BaseBody;
import com.wxy97.webhook.bean.PostData;
import com.wxy97.webhook.bean.wechat.WeChatTextMessage;
import com.wxy97.webhook.enums.WebhookEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 微信 Webhook 适配器
 * Author: wxy97.com
 * Date: 2026-02-26
 * Description: 将通用 webhook 事件格式转换为微信企业号消息格式
 */
@Slf4j
@Component
public class WeChatAdapter {

    /**
     * 将通用 webhook 事件转换为微信文本消息
     *
     * @param baseBody 通用 webhook 事件
     * @param mentionedList 提及的用户名列表
     * @param mentionedMobileList 提及的手机号列表
     * @return 微信文本消息
     */
    public WeChatTextMessage convertToWeChatText(BaseBody<?> baseBody, 
                                                   List<String> mentionedList, 
                                                   List<String> mentionedMobileList) {
        String content = formatEventAsText(baseBody);
        return new WeChatTextMessage(content, mentionedList, mentionedMobileList);
    }

    /**
     * 将通用 webhook 事件转换为微信文本消息（无提及）
     */
    public WeChatTextMessage convertToWeChatText(BaseBody<?> baseBody) {
        String content = formatEventAsText(baseBody);
        return new WeChatTextMessage(content);
    }

    /**
     * 格式化事件为文本内容
     */
    private String formatEventAsText(BaseBody<?> baseBody) {
        StringBuilder sb = new StringBuilder();
        
        // 添加事件类型标题
        sb.append("【").append(baseBody.getEventTypeName()).append("】\n");
        sb.append("时间: ").append(baseBody.getHookTime()).append("\n\n");
        
        // 根据事件类型格式化数据
        WebhookEventEnum eventType = baseBody.getEventType();
        
        if (eventType == WebhookEventEnum.NEW_POST && baseBody.getData() instanceof PostData) {
            PostData postData = (PostData) baseBody.getData();
            sb.append("标题: ").append(postData.getTitle()).append("\n");
            sb.append("作者: ").append(postData.getOwner()).append("\n");
            sb.append("发布时间: ").append(postData.getPublishTime()).append("\n");
            sb.append("链接: ").append(postData.getPermalink()).append("\n");
            sb.append("可见性: ").append(postData.getVisible());
        } else if (eventType == WebhookEventEnum.DELETE_POST) {
            sb.append("文章已被删除");
        } else if (eventType == WebhookEventEnum.NEW_COMMENT) {
            sb.append("有新评论");
        } else if (eventType == WebhookEventEnum.DELETE_COMMENT) {
            sb.append("评论已被删除");
        } else if (eventType == WebhookEventEnum.REPLY_COMMENT) {
            sb.append("有新回复");
        } else if (eventType == WebhookEventEnum.DELETE_REPLY_COMMENT) {
            sb.append("回复已被删除");
        } else if (eventType == WebhookEventEnum.NEW_DEVICE_LOGIN) {
            sb.append("检测到新设备登录");
        } else if (eventType == WebhookEventEnum.TEST_WEBHOOK) {
            sb.append("Webhook 测试成功");
        } else {
            // 默认格式化数据
            sb.append("数据: ").append(baseBody.getData().toString());
        }
        
        return sb.toString();
    }
}