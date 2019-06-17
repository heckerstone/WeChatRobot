package com.zk.WeChatRobot.controller;

import com.alibaba.fastjson.JSON;
import com.zk.WeChatRobot.MsgHandler.MessageHandler;
import com.zk.WeChatRobot.mapper.TempMaterialMapper;
import com.zk.WeChatRobot.pojo.TempMaterial;
import com.zk.WeChatRobot.router.WeChatRouter;
import com.zk.WeChatRobot.utils.HttpClientUtils;
import com.zk.WeChatRobot.utils.TuLingUtils;
import com.zk.WeChatRobot.utils.WeChatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

@RestController
@Slf4j
public class WeChatController {

    @Value("${token}")
    private String token;

    @Value("${wechat.uploaApi}")
    private String uploadUrl;

    @Autowired
    private WeChatRouter router;

    @Autowired
    private TempMaterialMapper mapper;

    @PostMapping(value = "/robotAnswer")
    public String getAnswer(HttpServletRequest request){
        log.info("进入post接口");
        Map<String, String> requestMap = TuLingUtils.getRequestMap(request);
        MessageHandler route = router.route(requestMap);
        String message = route.handleMessage(requestMap);
        return message;
    }

    @GetMapping(value = "/robotAnswer",produces = "text/plain;charset=utf-8")
    public String check(String timestamp, String nonce, String echostr, String signature){
        log.info("进入微信验证接口,%s,%s,%s,%s",timestamp,nonce,echostr,signature);
        if(TuLingUtils.sha1Result(timestamp,nonce,token,signature)){
           return echostr;
        }else{
            return "未知请求";
        }
    }

    @PostMapping(value = "/uploadTempMaterial")
    public String uploadTempMaterial(MultipartFile file){
        //首先获取文件的文件类型
        String mimeType = file.getContentType();
        String url = String.format(uploadUrl, WeChatUtils.getAccessToken(), mimeType);
        CommonsMultipartFile cf= (CommonsMultipartFile)file;
        DiskFileItem fi = (DiskFileItem)cf.getFileItem();
        File f = fi.getStoreLocation();
        String result = HttpClientUtils.uploadFile(url, f);
        TempMaterial tempMaterial = JSON.parseObject(result, TempMaterial.class);
        mapper.insert(tempMaterial);
        return "success";
    }

    @GetMapping("test")
    @ResponseBody
    public String test(){
        String accessToken = WeChatUtils.getAccessToken();
        System.out.println(accessToken);
        return accessToken;
    }
}
