package com.example.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogsAnalyzer {
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^\\]]+)\\]\\s+\"([^\"]*)\"\\s+(\\d+)\\s+(\\S+)"
    );

    public static void main(String[] args) {
        String fileName = "access.log";
        int total = 0;
        int errorCount = 0;
        Map<String, Integer> ipCount = new HashMap<>();
        int[] hourCount = new int[24];

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String line;
            while ((line = reader.readLine()) != null){
                if(line.trim().isEmpty()) continue; // 行为空
                Matcher m =  LINE_PATTERN.matcher(line);
                if(!m.find()) continue; // 脏行

                String ip = m.group(1);
                String time = m.group(2);
                int status = Integer.parseInt(m.group(4));

                total++;
                if(status >= 400) errorCount++;

                ipCount.merge(ip, 1, Integer::sum);

                String[] timeParts = time.split(":");
                if(timeParts.length >= 2){
                    try {
                        int hour = Integer.parseInt(timeParts[1]);
                        if(hour >= 0 && hour <= 23) hourCount[hour]++;
                    } catch (NumberFormatException e) {}
                }
            }

        } catch (IOException e) {
            System.out.println("读取日志文件失败：" + e.getMessage());
            return;
        }

        System.out.println("--- 网站日志分析报告 ---");

        System.out.println("[总览]");
        System.out.println("总请求数：" + total);

        System.out.println("[高频IP Top 3]");
        List<Map.Entry<String, Integer>> ipList = new ArrayList<>(ipCount.entrySet());
        ipList.sort((a, b) -> b.getValue() - a.getValue());
        int topN = Math.min(ipList.size(), 3);
        for(int i = 0; i < topN; i++){
            Map.Entry<String, Integer> e = ipList.get(i);
            System.out.println((i + 1) + ". " + e.getKey() + "（" + e.getValue() + " 次）");
        }

        System.out.println("[流量高峰时段]");
        int peakHour = 0;
        for (int h = 1; h < 24; h++) {
            if (hourCount[h] > hourCount[peakHour]) peakHour = h;
        }
        System.out.println("- 高峰时段：" + peakHour + "点（即 "
                + String.format("%02d", peakHour) + ":00 - "
                + String.format("%02d", peakHour) + ":59）");
        System.out.println("- 请求总数：" + hourCount[peakHour] + " 次");

        System.out.println("[错误请求分析]");
        System.out.println("- 错误请求（状态码 >= 400）：" + errorCount + " 次");
        String errorRate = total == 0
                ? "0.00%"
                : String.format("%.2f%%", errorCount * 100.0 / total);
        System.out.println("- 错误率：" + errorRate);

    }



}
