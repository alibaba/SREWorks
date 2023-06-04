package com.alibaba.sreworks.job.utils;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author jinghua.yjh
 */
public class StringUtil {

    public static boolean isEmpty(String string) {
        return string == null || "".equals(string) || "null".equals(string.toLowerCase());
    }

    public static Long toLong(String string) {
        return string == null ? null : Long.parseLong(string);
    }

    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

    public static String readResourceFile(String filename){
        InputStream inputStream = StringUtil.class.getClassLoader().getResourceAsStream(filename);

        // 读取文件内容
        Scanner scanner = new Scanner(inputStream);
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    public static StringBuilder intendLines(StringBuilder contents, String intendStr){
        String content = contents.toString();
        String[] lines = content.split(System.lineSeparator());
        StringBuilder newContents = new StringBuilder();
        for (String line : lines) {
            newContents.append(intendStr).append(line).append(System.lineSeparator());
        }
        return newContents;
    }

    public static String getMatchLine(String content, String searchText){
        String[] lines = content.split("\\r?\\n");
        int matchingLineIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(searchText)) {
                matchingLineIndex = i;
                break;
            }
        }
        if (matchingLineIndex >= 0) {
            return lines[matchingLineIndex];
        }else{
            return "";
        }
    }

}
