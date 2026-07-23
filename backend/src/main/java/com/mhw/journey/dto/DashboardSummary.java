package com.mhw.journey.dto;

public class DashboardSummary {
    private final long totalMemories;
    private final long restaurantCount;
    private final long tripCount;
    private final long cityCount;

    public DashboardSummary(long totalMemories, long restaurantCount, long tripCount, long cityCount) {
        this.totalMemories = totalMemories;
        this.restaurantCount = restaurantCount;
        this.tripCount = tripCount;
        this.cityCount = cityCount;
    }

    public long getTotalMemories() { return totalMemories; }
    public long getRestaurantCount() { return restaurantCount; }
    public long getTripCount() { return tripCount; }
    public long getCityCount() { return cityCount; }
}

