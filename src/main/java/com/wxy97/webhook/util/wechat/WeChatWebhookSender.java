package com.wxy97.webhook.util.wechat;

import com.wxy97.webhook.bean.wechat.WeChatTextMessage;
import com.wxy97.webhook.config.BasicSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * 微信 Webhook 发送器
 * Author: wxy97.com
 * Date: 2026-02-26
 * Description: 发送消息到微信企业号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeChatWebhookSender {

    private final WebClient.Builder webClientBuilder;
    private final ReactiveSettingFetcher reactiveSettingFetcher;

    /**
     * 发送微信文本消息
     *
     * @param message 微信文本消息
     * @return Mono<Void>
     */
    public Mono<Void> sendWeChatMessage(WeChatTextMessage message) {
        return reactiveSettingFetcher.fetchSetting(BasicSetting.GROUP_NAME, BasicSetting.class)
            .defaultIfEmpty(new BasicSetting())
            .flatMap(basicSetting -> {
                if (!basicSetting.getEnableWebhook() || basicSetting.getWebhookUrl() == null || basicSetting.getWebhookUrl().isEmpty()) {
                    log.warn("Webhook 未启用或 URL 未配置");
                    return Mono.empty();
                }

                return webClientBuilder.build()
                    .post()
                    .uri(basicSetting.getWebhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.info("微信消息发送成功"))
                    .doOnError(error -> log.error("微信消息发送失败", error))
                    .onErrorResume(WebClientException.class, ex -> {
                        log.error("微信 Webhook 请求异常: {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .then();
            })
            .onErrorResume(Exception.class, ex -> {
                log.error("发送微信消息异常", ex);
                return Mono.empty();
            });
    }
}