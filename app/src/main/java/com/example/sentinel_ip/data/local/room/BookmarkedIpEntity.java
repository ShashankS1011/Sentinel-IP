package com.example.sentinel_ip.data.local.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarked_ips")
public class BookmarkedIpEntity {

    @PrimaryKey
    @NonNull
    public String ipAddress;
    public int abuseConfidenceScore;
    public String countryCode;
    public String isp;
    public String domain;
    public long savedTimestamp;

    public BookmarkedIpEntity(@NonNull String ipAddress, int abuseConfidenceScore, String countryCode, String isp, String domain, long savedTimestamp) {
        this.ipAddress = ipAddress;
        this.abuseConfidenceScore = abuseConfidenceScore;
        this.countryCode = countryCode;
        this.isp = isp;
        this.domain = domain;
        this.savedTimestamp = savedTimestamp == 0 ? System.currentTimeMillis() : savedTimestamp;
    }
}
