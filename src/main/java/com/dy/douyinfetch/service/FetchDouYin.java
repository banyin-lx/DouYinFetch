package com.dy.douyinfetch.service;

import com.alibaba.fastjson.JSONObject;
import com.dy.douyinfetch.constant.Message;
import com.dy.douyinfetch.util.ConsoleOutputControl;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import entity.protobuf.DouYinHeader;
import entity.protobuf.DouYinMessage;
import org.apache.commons.codec.digest.DigestUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FetchDouYin {

    /**
     * 通过直播间url抓取roomId和ttwid
     *
     * @param url 直播间url
     * @return 数据
     * @throws IOException 异常
     */
    public static Map<String, String> fetchRoomId(String url) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .cookie("__ac_nonce", "063abcffa00ed8507d599")
                .timeout(3000)
                .execute();
        Document document = Jsoup.parse(response.body());
        Element body = document.body();
        Element roomData = body.getElementById("RENDER_DATA");
        Map<String, String> map = new HashMap<>();
        String ttwid = response.cookie("ttwid");
        map.put("ttwid", ttwid);
        if (null != roomData) {
            String decode = URLDecoder.decode(roomData.html(), "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(decode);
            String roomId = jsonObject.getJSONObject("app").getJSONObject("initialState").getJSONObject("roomStore").getJSONObject("roomInfo").getString("roomId");
            String userUniqueId = jsonObject.getJSONObject("app").getJSONObject("odin").getString("user_unique_id");
            map.put("roomId", roomId);
            map.put("userUniqueId", userUniqueId);
            return map;
        }
        return null;
    }

    /**
     * 监听弹幕等信息
     *
     * @param roomUrl 房间url
     * @throws IOException        异常
     * @throws URISyntaxException 异常
     */
    public static void startListening(String roomUrl) throws IOException, URISyntaxException {
        Map<String, String> paramMap = fetchRoomId(roomUrl);
        String roomId = paramMap.get("roomId");
        String userUniqueId = paramMap.get("userUniqueId");
        //拼接wss链接
        Map<String, String> wssMap = FetchDouYin.getWssUrl(paramMap.get("ttwid"), roomId);
        String wsUrl = wssMap.get("url") + "?";
        String internalExt = wssMap.get("internalExt");
        String req = "app_name=douyin_web&version_code=180800&webcast_sdk_version=1.3.0&update_version_code=1.3.0&compress=gzip&internal_ext=" + internalExt + "&cursor=d-1_u-1_h-1_t-1678883677711_r-1&host=https://live.douyin.com&aid=6383&live_id=1&did_rule=3&debug=false&maxCacheMessageNumber=20&endpoint=live_pc&support_wrds=1&im_path=/webcast/im/fetch/&user_unique_id=" + userUniqueId + "&device_platform=web&cookie_enabled=true&screen_width=1920&screen_height=1080&browser_language=zh-CN&browser_platform=Win32&browser_name=Mozilla&browser_online=true&tz_name=Asia/Shanghai&identity=audience&room_id=" + roomId + "&heartbeatDuration=0";
        //验签,先对这串数据进行md5加密后在走算法
        String sigStr = "live_id=1,aid=6383,version_code=180800,webcast_sdk_version=1.3.0,room_id=" + roomId + ",sub_room_id=,sub_channel_id=,did_rule=3,user_unique_id="
                + userUniqueId + ",device_platform=web,device_type=,ac=,identity=audience";
        //md5加密
        sigStr = DigestUtils.md5Hex(sigStr);
        //获取签名
        Map<String, String> douYinSignature = FetchDouYin.getDouYinSignature("https://live.douyin.com", sigStr);
        req = UriEncoder.encode(req);
        req += "&signature=" + douYinSignature.get("res");
        URI uri = new URI(wsUrl + req);
        System.out.println(uri);
        Map<String, String> map = new HashMap<>();
        map.put("Connection", "Upgrade");
        map.put("Upgrade", "websocket");
        map.put("Cookie", "ttwid=" + paramMap.get("ttwid"));
        WebSocketClient webSocketClient = new WebSocketClient(uri, map) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("连接成功");
            }

            @Override
            public void onMessage(String s) {
                System.out.println(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                System.out.println("关闭连接:" + s);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    entity.protobuf.DouYinHeader.PushFrame pushFrame = entity.protobuf.DouYinHeader.PushFrame.parseFrom(bytes);
                    //gzip解压
                    byte[] uncompress = uncompress(pushFrame.getPayload());
                    entity.protobuf.DouYinHeader.Response response = entity.protobuf.DouYinHeader.Response.parseFrom(uncompress);
                    List<entity.protobuf.DouYinHeader.Message> messagesListList = response.getMessagesListList();
                    //发送ack心跳信息
                    ackNeed(this, response, pushFrame);
                    for (entity.protobuf.DouYinHeader.Message message : messagesListList) {
                        //信息处理
                        messageHandle(message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Exception e) {
                System.out.println("错误" + e.getMessage());
            }
        };
        webSocketClient.connect();
    }

    /**
     * 发送ack心跳保活
     *
     * @param webSocketClient 客户端
     */
    private static void ackNeed(WebSocketClient webSocketClient, entity.protobuf.DouYinHeader.Response response, entity.protobuf.DouYinHeader.PushFrame oldPushFrame) {
        if (response.getNeedAck()) {
            entity.protobuf.DouYinHeader.PushFrame pushFrame = entity.protobuf.DouYinHeader.PushFrame.newBuilder()
                    .setPayloadType("ack")
                    .setPayload(response.getInternalExtBytes())
                    .setLogid(oldPushFrame.getLogid())
                    .build();
            webSocketClient.send(pushFrame.toByteArray());
        }
    }

    /**
     * 消息处理
     *
     * @param message 消息
     */
    private static void messageHandle(entity.protobuf.DouYinHeader.Message message) throws InvalidProtocolBufferException {
        String method = message.getMethod();
        Message realMe = Message.valueOf(method);
        switch (realMe) {
            case WebcastChatMessage:
                DouYinMessage.ChatMessage chatMessage = DouYinMessage.ChatMessage.parseFrom(message.getPayload());
                String content = chatMessage.getContent();
                DouYinMessage.User user = chatMessage.getUser();
                System.out.println(ConsoleOutputControl.getColoredOutputString("[" + user.getNickname() + "]:" + content,32));
                break;
            case WebcastMemberMessage:
                DouYinMessage.MemberMessage memberMessage = DouYinMessage.MemberMessage.parseFrom(message.getPayload());
                String nickname = memberMessage.getUser().getNickname();
                System.out.println(ConsoleOutputControl.getColoredOutputString(nickname+" 来了",33));;
                break;
            default:
                break;
        }
    }

    /**
     * gzip解压
     * @param sourceByte 数据
     * @return 结果
     */
    public static byte[] uncompress(ByteString sourceByte) {
        final byte[] bytes = sourceByte.toByteArray();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[5096];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            System.out.println("gzip uncompress error." + e);
        }

        return out.toByteArray();
    }

    /**
     * 获取wss链接中的参数
     *
     * @param ttwid  ttwid
     * @param roomId 房间号id
     * @return map
     * @throws IOException 异常
     */
    public static Map<String, String> getWssUrl(String ttwid, String roomId) throws IOException {
        String url = "https://live.douyin.com/webcast/im/fetch/?version_code=180800&resp_content_type=protobuf&did_rule=3&device_id=&device_platform=web&cookie_enabled=true&screen_width=1536&screen_height=864&browser_language=zh-CN&browser_platform=Win32&browser_name=Mozilla&browser_version=5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.57&browser_online=true&tz_name=Asia/Shanghai&aid=6383&live_id=1&app_name=douyin_web&debug=false&maxCacheMessageNumber=20&endpoint=live_pc&support_wrds=1&user_unique_id=7195428118517302842&identity=audience&room_id=" + roomId + "&last_rtt=0&fetch_rule=1&cursor=&internal_ext=";
        Map<String, String> map = new HashMap<>();
        map.put("ttwid", ttwid);
        map.put("sid_tt", "29929c8be74cfb131e91febc3e86681a");
        Connection.Response execute = Jsoup.connect(url)
                .cookies(map)
                .ignoreContentType(true)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .execute();
        DouYinHeader.Response response = DouYinHeader.Response.parseFrom(execute.bodyAsBytes());
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("url", response.getPushServer());
        returnMap.put("internalExt", response.getInternalExt());
        return returnMap;
    }

    /**
     * 抖音签名(通过chormDriver获取)
     * @param url
     * @param param
     * @return
     */
    public static Map<String, String> getDouYinSignature(String url, String param) {
        // 参数配置
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\src\\main\\resources\\chromedriver.exe");
        ChromeOptions option = new ChromeOptions();
        option.addArguments("--remote-allow-origins=*");
        option.addArguments("headless"); // 无界面参数
        option.addArguments("no-sandbox"); // 禁用沙盒
        // 通过ChromeOptions的setExperimentalOption方法，传下面两个参数来禁止掉谷歌受自动化控制的信息栏
        option.setExperimentalOption("useAutomationExtension", false);
        option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        ChromeDriver driver = null;
        Map<String, String> map = new HashMap<>();
        try {
            driver = new ChromeDriver(option);
            driver.get(url);
            //通过向页面插入js获取签名结果
            String js = "{\"X-MS-STUB\": \""+param+"\" }";
            Map<String, String> o = (Map<String, String>) ((JavascriptExecutor) driver).executeScript("return window.byted_acrawler.frontierSign("+js+")");
            map.put("res", o.get("X-Bogus"));
        }finally {
            driver.close();
            driver.quit();
        }
        return map;
    }

}
