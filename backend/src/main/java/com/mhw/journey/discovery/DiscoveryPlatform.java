package com.mhw.journey.discovery;

public enum DiscoveryPlatform {
    XIAOHONGSHU("小红书", "xiaohongshu.com"),
    DOUYIN("抖音", "douyin.com"),
    WEIBO("微博", "weibo.com"),
    ZHIHU("知乎", "zhihu.com");

    private final String label;
    private final String domain;
    DiscoveryPlatform(String label, String domain) { this.label = label; this.domain = domain; }
    public String getLabel() { return label; }
    public String getDomain() { return domain; }
}
