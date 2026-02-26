package com.wxy97.webhook.strategy;

import com.wxy97.webhook.adapter.WeChatAdapter;
import com.wxy97.webhook.bean.BaseBody;
import com.wxy97.webhook.bean.PostData;
import com.wxy97.webhook.bean.wechat.WeChatTextMessage;
import com.wxy97.webhook.enums.WebhookEventEnum;
import com.wxy97.webhook.util.DateUtil;
import com.wxy97.webhook.util.wechat.WeChatWebhookSender;
import com.wxy97.webhook.watch.ExtensionChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * 微信 Webhook 策略
 * Author: wxy97.com
 * Date: 2026-02-26
 * Description: 处理事件并转换为微信消息格式
 */
@Slf4j
@StrategyKind("Post")
@Component
@RequiredArgsConstructor
public class WeChatStrategy implements ExtensionStrategy {

    private final WeChatAdapter weChatAdapter;
    private final WeChatWebhookSender weChatWebhookSender;

    @Override
    public void process(ExtensionChangedEvent event, ReactiveExtensionClient reactiveExtensionClient) {
        Extension extension = event.getExtension();
        ExtensionChangedEvent.EventType eventType = event.getEventType();

        try {
            // 只处理 Post 类型的事件
            if (!(extension instanceof Post)) {
                return;
            }

            Post post = (Post) extension;
            
            // 构建 BaseBody 对象
            BaseBody<PostData> baseBody = new BaseBody<>();
            PostData postData = PostData.convertToPostData(post);
            
            // 根据事件类型设置事件枚举
            if (eventType == ExtensionChangedEvent.EventType.ADDED) {
                baseBody.setEventType(WebhookEventEnum.NEW_POST);
                baseBody.setEventTypeName(WebhookEventEnum.NEW_POST.getDescription());
            } else if (eventType == ExtensionChangedEvent.EventType.UPDATED) {
                baseBody.setEventType(WebhookEventEnum.NEW_POST);
                baseBody.setEventTypeName("更新文章");
            } else if (eventType == ExtensionChangedEvent.EventType.DELETED) {
                baseBody.setEventType(WebhookEventEnum.DELETE_POST);
                baseBody.setEventTypeName(WebhookEventEnum.DELETE_POST.getDescription());
            }
            
            baseBody.setHookTime(DateUtil.formatNow());
            baseBody.setData(postData);
            
            // 转换为微信文本消息
            WeChatTextMessage weChatMessage = weChatAdapter.convertToWeChatText(baseBody);
            
            // 发送微信消息
            weChatWebhookSender.sendWeChatMessage(weChatMessage)
                .subscribe(
                    () -> log.info("微信消息已发送"),
                    error -> log.error("微信消息发送失败", error)
                );
        } catch (Exception e) {
            log.error("处理微信 Webhook 异常", e);
        }
    }
}