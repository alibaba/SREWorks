package com.alibaba.tesla.appmanager.common.util;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stop.DestroyProcessStopper;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 命令工具类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
public class CommandUtil {

    /**
     * 在本机运行命令，并返回运行结果
     *
     * @param commands 命令数组
     * @param envMap   环境变量字典
     * @return 命令执行结果
     */
    public static String runLocalCommand(String[] commands, Map<String, String> envMap, File pwd) {
        // 前置安全过滤
        List<String> safeCommands = new ArrayList<>();
        for (String command : commands) {
            String safeCommand = trimCmdwithCh(command);
            if (safeCommand == null) {
                throw new AppException(AppErrorCode.COMMAND_ERROR,
                        String.format("unsafe command|command=%s", String.join(" ", commands)));
            }
            safeCommands.add(safeCommand);
        }

        try {
            ProcessExecutor process = new ProcessExecutor()
                    .command(safeCommands.toArray(new String[0]))
                    .environment(envMap)
                    .redirectOutput(Slf4jStream.ofCaller().asInfo())
                    .redirectErrorStream(true)
                    .timeout(120, TimeUnit.MINUTES)
                    .stopper(DestroyProcessStopper.INSTANCE)
                    .readOutput(true);

            if (pwd != null) {
                process.directory(pwd);
            }

            ProcessResult result = process.execute();
            int retCode = result.getExitValue();
            String output = result.outputUTF8();
            if (retCode != 0) {
                throw new AppException(AppErrorCode.COMMAND_ERROR,
                        String.format("action=runLocalCommand|command=%s|retCode=%d|output=%s",
                                String.join(" ", commands), retCode, output));
            }
            log.info("action=runLocalCommand|command={}|retCode={}", String.join(" ", commands), retCode);
            return output;
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new AppException(AppErrorCode.COMMAND_ERROR,
                    String.format("action=runLocalCommand|command=%s", String.join(" ", commands)), e);
        }
    }

    public static String runLocalCommand(String[] commands, File pwd) {
        return runLocalCommand(commands, new HashMap<>(), pwd);
    }

    public static String runLocalCommand(String[] commands) {
        return runLocalCommand(commands, new HashMap<>(), null);
    }

    /**
     * 将需要执行的命令转换为 bash 执行 (一般用于通配符及重定向等)
     *
     * @param commands 命令列表
     * @return bash command 命令列表
     */
    public static String[] getBashCommand(String[] commands) {
        return new String[]{
                "bash",
                "-c",
                String.join(" ", commands).replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
        };
    }

    /**
     * 安全过滤函数
     *
     * @param slice 字符串
     * @return 安全字符串
     */
    public static String trimCmdwithCh(String slice) {
        if (slice == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : slice.toCharArray()) {
            if ((c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || (c >= 'A' && c <= 'Z')
                    || c == '_' || c == '-'
                    || c == ',' || c == '~'
                    || c == '/' || c == '\\'
                    || c == '*' || c == '['
                    || c == ']' || c == '!'
                    || c == '\'' || c == '"'
                    || c == '=' || c == '>'
                    || c == '{' || c == '}'
                    || c == '(' || c == ')'
                    || c == ' ' || c == '.'
                    || c == ':' || c == '@'
                    || (c >= 0x4e00 && c <= 0x9fbb)) {
                sb.append(c);
            } else {
                return null;
            }
        }
        return sb.toString();
    }
}
