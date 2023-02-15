package com.alibaba.tesla.appmanager.encryption.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 3.18 专有云配置文件加解密专用
 * <p>
 * https://yuque.antfin.com/zhuoyu.yzy/omo2sv/csmf6gv8rdsy3bci?singleDoc#Azt5Y
 * <p>
 * curl http://localhost/api/v1/configs --unix-socket /var/run/proxyssl/pssl.sockrest -H "Content-Type: application/octet-stream" -X POST -d'"key":"value"'
 *
 * @author zhanghanqi.zhq@alibaba-inc.com
 */
@Slf4j
public class DecryptUtil {

    public static final String URL_PATH = "/api/v1/configs";

    public static final String SOCKET_PATH = "/var/run/proxyssl/pssl.sockrest";

    public static final String ENCRYPT_FILE_PATH = "/encryptfile/config/config_encrypt.json";

    public DecryptUtil() {
    }

    public static void decryptAndSetEnv() {
        try {
            Map<String, String> encryptedMap = readJsonFileToMap();
            log.info("Encrypted content {}", JSONObject.toJSONString(encryptedMap));

            for (Map.Entry<String, String> entry : encryptedMap.entrySet()) {
                if (decrypt(entry)) {
                    log.info("Decrypt successful! key: {}, value: {}", entry.getKey(), entry.getValue());
                    if (setEnv(entry.getKey(), entry.getValue())) {
                        log.info("SetEnv successful!");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Decrypt and set env failed, exception={}", ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 将加密 json 文件读取成 Map
     *
     * @return
     */
    private static Map<String, String> readJsonFileToMap() throws IOException {
        String content = FileUtils.readFileToString(new File(ENCRYPT_FILE_PATH), "UTF-8");
        return JSONObject.parseObject(content, new TypeReference<>() {
        });
    }

    /**
     * 创建 UnixSocketChannel
     *
     * @return
     */
    private static UnixSocket createUnixSocket() throws IOException {
        File sockFile = new File(SOCKET_PATH);
        UnixSocketAddress unixSocketAddress = new UnixSocketAddress(sockFile);
        UnixSocketChannel unixSocketChannel = UnixSocketChannel.open(unixSocketAddress);
        return new UnixSocket(unixSocketChannel);
    }

    /**
     * 调用底座接口解密内容
     *
     * @param entry
     * @return
     */
    private static boolean decrypt(Map.Entry<String, String> entry) {
        try {
            UnixSocket socket = createUnixSocket();

            String data = entry.getValue() + "\r\r";
            PrintWriter w = new PrintWriter(socket.getOutputStream());
            w.println("POST " + URL_PATH + " HTTP/1.1");
            w.println("Host: http");
            w.println("Content-Type: application/octet-stream");
            w.println("Content-length: " + data.length());
            w.println("Connection: close");
            w.println("Accept: */*");
            w.println("");
            w.println(data);
            w.println("");
            w.flush();

            socket.shutdownOutput();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder responseStr = new StringBuilder();
            String line;
            String decodeResult = "";
            while ((line = br.readLine()) != null) {
                responseStr.append(line).append("\n");
                if (line.trim().length() > 0) {
                    decodeResult = line;
                }
            }

            w.close();
            br.close();
            socket.close();

            if (!responseStr.toString().contains("HTTP/1.1 200 OK")) {
                throw new Exception(decodeResult);
            }

            entry.setValue(decodeResult);
            return true;

        } catch (Exception e) {
            log.warn("Decrypt content {}:{} failed, exception={}", entry.getKey(), entry.getValue(), ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     * 设置环境变量
     *
     * @param key
     * @param value
     */
    private static boolean setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Field field = env.getClass().getDeclaredField("m");
            field.setAccessible(true);
            ((Map<String, String>) field.get(env)).put(key, value);
            return StringUtils.equals(System.getenv(key), value);
        } catch (Exception e) {
            log.warn("Set env {}:{} failed, exception={}", key, value, ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     * 通过 run.sh 中注入环境变量后，查看
     */
    public static void logOutAllEncryptEnv() {
        try {
            Map<String, String> encryptEnv = readJsonFileToMap();
            for (Map.Entry<String, String> env : encryptEnv.entrySet()) {
                log.info("Env key: {}, decrypt value: {}", env.getKey(), System.getenv(env.getKey()));
            }
        } catch (Exception e) {
            log.warn("Get all encrypt env failed, exception={}", ExceptionUtils.getStackTrace(e));
        }
    }
}
