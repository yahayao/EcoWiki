package com.ecowiki.util;

import jakarta.annotation.PostConstruct;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;

@Component
public class Ip2RegionService {

    private Searcher searcherV4;
    private Searcher searcherV6;

    private static final String V4_CLASSPATH = "ipdb/ip2region_v4.xdb";
    private static final String V6_CLASSPATH = "ipdb/ip2region_v6.xdb";

    @PostConstruct
    public void init() {
        try {
            // 将 classpath 中的 v4 xdb 复制到临时文件
            File v4TempFile = copyToTempFile(V4_CLASSPATH, "ip2region_v4", ".xdb");
            // 使用 loadContentFromFile 加载临时文件，得到 LongByteArray
            LongByteArray v4Buffer = Searcher.loadContentFromFile(v4TempFile.getPath());
            searcherV4 = Searcher.newWithBuffer(Version.IPv4, v4Buffer);

            // 同样处理 v6
            File v6TempFile = copyToTempFile(V6_CLASSPATH, "ip2region_v6", ".xdb");
            LongByteArray v6Buffer = Searcher.loadContentFromFile(v6TempFile.getPath());
            searcherV6 = Searcher.newWithBuffer(Version.IPv6, v6Buffer);

            // 可选的：删除临时文件（但保留也可以，后续不再使用）
            // v4TempFile.deleteOnExit();
            // v6TempFile.deleteOnExit();
        } catch (Exception e) {
            throw new RuntimeException("ip2region xdb 加载失败", e);
        }
    }

    /**
     * 将 classpath 资源复制到临时文件
     */
    private File copyToTempFile(String classpath, String prefix, String suffix) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpath);
        try (InputStream is = resource.getInputStream()) {
            File tempFile = Files.createTempFile(prefix, suffix).toFile();
            tempFile.deleteOnExit(); // JVM 退出时自动删除
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
            return tempFile;
        }
    }

    /**
     * 根据IP地址查询归属地信息（完整格式）
     * @param ip IPv4 或 IPv6 地址
     * @return 国家|省份|城市|ISP|iso-alpha2-code，失败返回 "未知位置"
     */
    public String getRegion(String ip) {
        try {
            // 自动判断 IP 版本
            InetAddress inetAddress = InetAddress.getByName(ip);
            if (inetAddress.getAddress().length == 4) {
                return searcherV4.search(ip);
            } else {
                return searcherV6.search(ip);
            }
        } catch (Exception e) {
            return "未知位置";
        }
    }

    /**
     * 获取城市名（简化版）
     */
    public String getCity(String ip) {
        String region = getRegion(ip);
        if (StringUtils.isEmpty(region) || "未知位置".equals(region)) {
            return region;
        }
        String[] parts = region.split("\\|");
        // v3.x 格式：国家|省份|城市|ISP|code
        return parts.length >= 3 ? parts[2] : region;
    }

    /**
     * 获取结构化地域信息
     */
    public RegionInfo getRegionInfo(String ip) {
        String region = getRegion(ip);
        RegionInfo info = new RegionInfo();

        if (!StringUtils.isEmpty(region) && !"未知位置".equals(region)) {
            String[] parts = region.split("\\|");
            if (parts.length >= 5) {
                info.setCountry(parts[0]);
                info.setProvince(parts[1]);
                info.setCity(parts[2]);
                info.setIsp(parts[3]);
                info.setCountryCode(parts[4]);
            }
        }
        return info;
    }

    // 内部类：地域信息
    public static class RegionInfo {
        private String country;      // 国家
        private String province;     // 省份
        private String city;         // 城市
        private String isp;          // 运营商
        private String countryCode;  // 国家代码

        // getter / setter （省略，请自行生成）
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }
    }
}