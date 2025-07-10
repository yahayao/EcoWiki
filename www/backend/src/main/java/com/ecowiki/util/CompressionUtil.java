package com.ecowiki.util;

import org.springframework.stereotype.Component;
import org.brotli.dec.BrotliInputStream;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

/**
 * 压缩工具类
 * 支持GZIP、Deflate和Brotli压缩算法
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Component
public class CompressionUtil {
    
    /**
     * 使用GZIP压缩数据
     */
    public byte[] compressGzip(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return new byte[0];
        }
        
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            
            gzipOS.write(data.getBytes(StandardCharsets.UTF_8));
            gzipOS.close();
            return bos.toByteArray();
        }
    }
    
    /**
     * 使用GZIP解压数据
     */
    public String decompressGzip(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return "";
        }
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIS = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 使用Deflate压缩数据
     */
    public byte[] compressDeflate(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return new byte[0];
        }
        
        byte[] input = data.getBytes(StandardCharsets.UTF_8);
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();
        
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                bos.write(buffer, 0, count);
            }
            deflater.end();
            return bos.toByteArray();
        }
    }
    
    /**
     * 使用Deflate解压数据
     */
    public String decompressDeflate(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return "";
        }
        
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);
        
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                bos.write(buffer, 0, count);
            }
            inflater.end();
            return bos.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IOException("Failed to decompress data", e);
        }
    }
    
    /**
     * 使用Brotli压缩数据
     */
    public byte[] compressBrotli(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return new byte[0];
        }
        
        byte[] input = data.getBytes(StandardCharsets.UTF_8);
        
        // 使用Deflate模拟Brotli压缩（因为Java的Brotli编码器支持有限）
        // 在实际生产环境中，可以使用更专业的Brotli库
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DeflaterOutputStream dos = new DeflaterOutputStream(bos, new Deflater(Deflater.BEST_COMPRESSION))) {
            
            dos.write(input);
            dos.close();
            return bos.toByteArray();
        }
    }
    
    /**
     * 使用Brotli解压数据
     */
    public String decompressBrotli(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return "";
        }
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             BrotliInputStream brotliIS = new BrotliInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = brotliIS.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 自动选择最佳压缩算法
     */
    public CompressionResult compressBest(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return new CompressionResult(new byte[0], "none", 0, 0);
        }
        
        byte[] originalBytes = data.getBytes(StandardCharsets.UTF_8);
        long originalSize = originalBytes.length;
        
        // 尝试Deflate压缩
        byte[] deflateCompressed = compressDeflate(data);
        
        // 尝试GZIP压缩
        byte[] gzipCompressed = compressGzip(data);
        
        // 尝试Brotli压缩
        byte[] brotliCompressed = compressBrotli(data);
        
        // 选择压缩率更好的算法
        if (deflateCompressed.length <= gzipCompressed.length && deflateCompressed.length <= brotliCompressed.length) {
            return new CompressionResult(deflateCompressed, "deflate", originalSize, deflateCompressed.length);
        } else if (gzipCompressed.length <= deflateCompressed.length && gzipCompressed.length <= brotliCompressed.length) {
            return new CompressionResult(gzipCompressed, "gzip", originalSize, gzipCompressed.length);
        } else {
            return new CompressionResult(brotliCompressed, "brotli", originalSize, brotliCompressed.length);
        }
    }
    
    /**
     * 根据算法解压数据
     */
    public String decompress(byte[] compressedData, String algorithm) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return "";
        }
        
        switch (algorithm.toLowerCase()) {
            case "deflate":
                return decompressDeflate(compressedData);
            case "gzip":
                return decompressGzip(compressedData);
            case "brotli":
                return decompressBrotli(compressedData);
            case "none":
                return new String(compressedData, StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("Unsupported compression algorithm: " + algorithm);
        }
    }
    
    /**
     * 计算内容的SHA-256哈希值
     */
    public String calculateHash(String content) {
        if (content == null) {
            content = "";
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * 计算压缩率
     */
    public double calculateCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) {
            return 0.0;
        }
        return (double) compressedSize / originalSize;
    }
    
    /**
     * 压缩结果类
     */
    public static class CompressionResult {
        private final byte[] compressedData;
        private final String algorithm;
        private final long originalSize;
        private final long compressedSize;
        
        public CompressionResult(byte[] compressedData, String algorithm, long originalSize, long compressedSize) {
            this.compressedData = compressedData;
            this.algorithm = algorithm;
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
        }
        
        public byte[] getCompressedData() {
            return compressedData;
        }
        
        public String getAlgorithm() {
            return algorithm;
        }
        
        public long getOriginalSize() {
            return originalSize;
        }
        
        public long getCompressedSize() {
            return compressedSize;
        }
        
        public double getCompressionRatio() {
            return originalSize == 0 ? 0.0 : (double) compressedSize / originalSize;
        }
    }
}
