package com.ecowiki.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * 差异计算工具类
 * 实现了Myers diff算法的简化版本，用于计算文本差异
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Component
public class DiffUtil {
    
    private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\r?\n");
    
    /**
     * 计算两个文本之间的差异
     */
    public DiffResult calculateDiff(String oldText, String newText) {
        if (oldText == null) oldText = "";
        if (newText == null) newText = "";
        
        String[] oldLines = LINE_SPLIT_PATTERN.split(oldText);
        String[] newLines = LINE_SPLIT_PATTERN.split(newText);
        
        List<DiffLine> diffLines = computeDiff(oldLines, newLines);
        
        // 计算差异大小
        int originalSize = oldText.length();
        int addedChars = 0;
        int removedChars = 0;
        
        for (DiffLine line : diffLines) {
            if (line.getType() == DiffType.ADDED) {
                addedChars += line.getContent().length();
            } else if (line.getType() == DiffType.REMOVED) {
                removedChars += line.getContent().length();
            }
        }
        
        // 计算差异比例
        double diffRatio = originalSize == 0 ? 1.0 : (double) (addedChars + removedChars) / originalSize;
        
        return new DiffResult(diffLines, diffRatio, addedChars, removedChars);
    }
    
    /**
     * 应用差异到基础文本
     */
    public String applyDiff(String baseText, List<DiffLine> diffLines) {
        if (baseText == null) baseText = "";
        
        String[] baseLines = LINE_SPLIT_PATTERN.split(baseText);
        List<String> resultLines = new ArrayList<>();
        
        // 将差异按行号分组
        Map<Integer, List<DiffLine>> diffsByLine = new HashMap<>();
        for (DiffLine diff : diffLines) {
            diffsByLine.computeIfAbsent(diff.getLineNumber(), k -> new ArrayList<>()).add(diff);
        }
        
        // 对每行的差异按类型排序：UNCHANGED -> ADDED -> REMOVED
        for (List<DiffLine> lineDiffs : diffsByLine.values()) {
            lineDiffs.sort((a, b) -> {
                int priorityA = a.getType() == DiffType.UNCHANGED ? 0 : 
                               a.getType() == DiffType.ADDED ? 1 : 2;
                int priorityB = b.getType() == DiffType.UNCHANGED ? 0 : 
                               b.getType() == DiffType.ADDED ? 1 : 2;
                return Integer.compare(priorityA, priorityB);
            });
        }
        
        // 处理所有行，包括基础行和差异行
        int maxLine = Math.max(baseLines.length - 1, 
                               diffLines.stream().mapToInt(DiffLine::getLineNumber).max().orElse(-1));
        
        for (int lineNum = 0; lineNum <= maxLine; lineNum++) {
            List<DiffLine> lineDiffs = diffsByLine.get(lineNum);
            
            if (lineDiffs == null || lineDiffs.isEmpty()) {
                // 没有差异，直接复制基础行
                if (lineNum < baseLines.length) {
                    resultLines.add(baseLines[lineNum]);
                }
            } else {
                // 先处理UNCHANGED，再处理ADDED
                List<DiffLine> addedLines = new ArrayList<>();
                
                for (DiffLine diff : lineDiffs) {
                    switch (diff.getType()) {
                        case UNCHANGED:
                            if (lineNum < baseLines.length) {
                                resultLines.add(baseLines[lineNum]);
                            }
                            break;
                        case ADDED:
                            addedLines.add(diff);
                            break;
                        case REMOVED:
                            // 移除行，不做处理
                            break;
                    }
                }
                
                // 添加所有新增的行
                for (DiffLine addedLine : addedLines) {
                    resultLines.add(addedLine.getContent());
                }
                
                // 如果没有UNCHANGED和REMOVED，但是基础行存在，则复制基础行
                if (lineDiffs.stream().noneMatch(d -> d.getType() == DiffType.UNCHANGED || d.getType() == DiffType.REMOVED) 
                    && lineNum < baseLines.length) {
                    resultLines.add(baseLines[lineNum]);
                }
            }
        }
        
        String result = String.join("\n", resultLines);
        
        return result;
    }
    
    /**
     * 将差异编码为字符串
     */
    public String encodeDiff(List<DiffLine> diffLines) {
        StringBuilder sb = new StringBuilder();
        
        for (DiffLine line : diffLines) {
            sb.append(line.getType().getSymbol())
              .append(line.getLineNumber())
              .append(":")
              .append(line.getContent())
              .append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 从字符串解码差异
     */
    public List<DiffLine> decodeDiff(String encodedDiff) {
        if (encodedDiff == null || encodedDiff.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DiffLine> diffLines = new ArrayList<>();
        String[] lines = LINE_SPLIT_PATTERN.split(encodedDiff);
        
        for (String line : lines) {
            if (line.length() < 3) continue;
            
            char typeSymbol = line.charAt(0);
            int colonIndex = line.indexOf(':', 1);
            if (colonIndex == -1) continue;
            
            try {
                int lineNumber = Integer.parseInt(line.substring(1, colonIndex));
                String content = line.substring(colonIndex + 1);
                DiffType type = DiffType.fromSymbol(typeSymbol);
                
                diffLines.add(new DiffLine(type, lineNumber, content));
            } catch (NumberFormatException e) {
                // 跳过无效行
            }
        }
        
        return diffLines;
    }
    
    /**
     * 计算差异大小百分比
     */
    public double calculateDiffPercentage(String oldText, String newText) {
        DiffResult result = calculateDiff(oldText, newText);
        return result.getDiffRatio();
    }
    
    /**
     * 判断是否应该存储为完整版本而不是差异
     */
    public boolean shouldStoreAsFullVersion(String oldText, String newText, double threshold) {
        double diffRatio = calculateDiffPercentage(oldText, newText);
        return diffRatio > threshold;
    }
    
    /**
     * 简化的LCS算法实现
     */
    private List<DiffLine> computeDiff(String[] oldLines, String[] newLines) {
        List<DiffLine> result = new ArrayList<>();
        
        // 使用简单的匹配算法
        int oldIndex = 0;
        int newIndex = 0;
        
        while (oldIndex < oldLines.length || newIndex < newLines.length) {
            if (oldIndex >= oldLines.length) {
                // 只剩新行，全部标记为添加，行号使用新文本的行号
                result.add(new DiffLine(DiffType.ADDED, newIndex, newLines[newIndex]));
                newIndex++;
            } else if (newIndex >= newLines.length) {
                // 只剩旧行，全部标记为删除
                result.add(new DiffLine(DiffType.REMOVED, oldIndex, oldLines[oldIndex]));
                oldIndex++;
            } else if (oldLines[oldIndex].equals(newLines[newIndex])) {
                // 行相同，标记为未变
                result.add(new DiffLine(DiffType.UNCHANGED, oldIndex, oldLines[oldIndex]));
                oldIndex++;
                newIndex++;
            } else {
                // 查找下一个匹配的行
                int nextMatchOld = findNextMatch(oldLines, oldIndex, newLines[newIndex]);
                int nextMatchNew = findNextMatch(newLines, newIndex, oldLines[oldIndex]);
                
                if (nextMatchOld != -1 && (nextMatchNew == -1 || nextMatchOld - oldIndex <= nextMatchNew - newIndex)) {
                    // 删除旧行直到匹配
                    while (oldIndex < nextMatchOld) {
                        result.add(new DiffLine(DiffType.REMOVED, oldIndex, oldLines[oldIndex]));
                        oldIndex++;
                    }
                } else if (nextMatchNew != -1) {
                    // 添加新行直到匹配，行号使用插入位置（当前oldIndex）
                    while (newIndex < nextMatchNew) {
                        result.add(new DiffLine(DiffType.ADDED, oldIndex, newLines[newIndex]));
                        newIndex++;
                    }
                } else {
                    // 没有找到匹配，当作修改处理
                    result.add(new DiffLine(DiffType.REMOVED, oldIndex, oldLines[oldIndex]));
                    result.add(new DiffLine(DiffType.ADDED, oldIndex, newLines[newIndex]));
                    oldIndex++;
                    newIndex++;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 查找下一个匹配的行
     */
    private int findNextMatch(String[] lines, int startIndex, String target) {
        for (int i = startIndex + 1; i < lines.length && i < startIndex + 10; i++) {
            if (lines[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 差异类型枚举
     */
    public enum DiffType {
        ADDED('+'),
        REMOVED('-'),
        UNCHANGED(' ');
        
        private final char symbol;
        
        DiffType(char symbol) {
            this.symbol = symbol;
        }
        
        public char getSymbol() {
            return symbol;
        }
        
        public static DiffType fromSymbol(char symbol) {
            for (DiffType type : values()) {
                if (type.symbol == symbol) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown diff type symbol: " + symbol);
        }
    }
    
    /**
     * 差异行类
     */
    public static class DiffLine {
        private final DiffType type;
        private final int lineNumber;
        private final String content;
        
        public DiffLine(DiffType type, int lineNumber, String content) {
            this.type = type;
            this.lineNumber = lineNumber;
            this.content = content;
        }
        
        public DiffType getType() {
            return type;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public String getContent() {
            return content;
        }
        
        @Override
        public String toString() {
            return type.getSymbol() + lineNumber + ":" + content;
        }
    }
    
    /**
     * 差异结果类
     */
    public static class DiffResult {
        private final List<DiffLine> diffLines;
        private final double diffRatio;
        private final int addedChars;
        private final int removedChars;
        
        public DiffResult(List<DiffLine> diffLines, double diffRatio, int addedChars, int removedChars) {
            this.diffLines = diffLines;
            this.diffRatio = diffRatio;
            this.addedChars = addedChars;
            this.removedChars = removedChars;
        }
        
        public List<DiffLine> getDiffLines() {
            return diffLines;
        }
        
        public double getDiffRatio() {
            return diffRatio;
        }
        
        public int getAddedChars() {
            return addedChars;
        }
        
        public int getRemovedChars() {
            return removedChars;
        }
        
        public boolean isLargeChange(double threshold) {
            return diffRatio > threshold;
        }
    }
}
