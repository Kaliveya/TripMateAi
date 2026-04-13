package com.sean.trip.tripmateai.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class AnalyzeMdUtil {

    // 每个chunk最大长度
    private static final int MAX_CHUNK_SIZE = 500;

    // chunk之间的重叠长度（提升语义连续性）
    private static final int OVERLAP_SIZE = 100;

    public static List<String> analyzeMd(String mdFilePath) {
        List<String> chunks = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(mdFilePath));

            List<String> currentBlock = new ArrayList<>();

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                // 遇到标题（# 开头） -> 切块
                if (isHeader(line)) {
                    flushBlock(currentBlock, chunks);
                    currentBlock.clear();
                }

                currentBlock.add(line);
            }

            // 最后一个块
            flushBlock(currentBlock, chunks);

        } catch (IOException e) {
            throw new RuntimeException("读取MD文件失败", e);
        }

        return chunks;
    }

    /**
     * 判断是否是标题
     */
    private static boolean isHeader(String line) {
        return line.startsWith("#");
    }

    /**
     * 将block切成多个chunk（带overlap）
     */
    private static void flushBlock(List<String> block, List<String> chunks) {
        if (block.isEmpty()) {
            return;
        }

        String content = String.join("\n", block);

        int start = 0;
        int length = content.length();

        while (start < length) {
            int end = Math.min(start + MAX_CHUNK_SIZE, length);

            String chunk = content.substring(start, end);
            chunks.add(chunk);

            // 下一段带 overlap
            start += (MAX_CHUNK_SIZE - OVERLAP_SIZE);
        }
    }
}