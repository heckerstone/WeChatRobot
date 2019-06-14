package com.zk.WeChatRobot.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zk.WeChatRobot.utils.HttpClientUtils;
import com.zk.WeChatRobot.utils.WeChatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class WeChatScheduler {

    @Value(("${wechat.appID}"))
    private String appID;
    @Value("${wechat.appsecret}")
    private String appsecret;
    @Value("${wechat.accessToken.api}")
    private String accessTokenApi;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("spring-task-thread");
        return scheduler;
    }

    @Scheduled(fixedDelay = 7100000)
    protected void refreshAccessToken(){
        //访问微信的接口获取access_token
        String url = String.format(accessTokenApi, appID, appsecret);
        String response = HttpClientUtils.sendGetRequest(url);
        JSONObject jsonObject = JSON.parseObject(response);
        String access_token = jsonObject.getString("access_token");
        WeChatUtils.access_token = access_token;
    }
}
