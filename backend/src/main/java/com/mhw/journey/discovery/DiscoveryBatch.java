package com.mhw.journey.discovery;

import com.mhw.journey.model.DiscoveryRecord;
import java.time.LocalDateTime;
import java.util.List;

public class DiscoveryBatch {
    private final String status;
    private final String message;
    private final List<String> connectedSources;
    private final List<String> unavailableSources;
    private final LocalDateTime refreshedAt;
    private final List<DiscoveryRecord> records;

    public DiscoveryBatch(String status, String message, List<String> connectedSources, List<String> unavailableSources,
                          LocalDateTime refreshedAt, List<DiscoveryRecord> records) {
        this.status=status; this.message=message; this.connectedSources=connectedSources; this.unavailableSources=unavailableSources;
        this.refreshedAt=refreshedAt; this.records=records;
    }
    public String getStatus(){return status;} public String getMessage(){return message;}
    public List<String> getConnectedSources(){return connectedSources;} public List<String> getUnavailableSources(){return unavailableSources;}
    public LocalDateTime getRefreshedAt(){return refreshedAt;} public List<DiscoveryRecord> getRecords(){return records;}
}
