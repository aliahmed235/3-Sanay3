package com.sany3.graduation_project.entites;

/**
 * Types of penalties that reduce provider reputation
 */
public enum RatingPenaltyType {

    /**
     * Provider accepted offer then cancelled
     * Penalty: -0.5 stars
     *
     * Why: Wastes customer's time, unreliable
     * Timeline:
     *   - Provider: "I'll fix it"
     *   - 5 minutes later: "Nevermind, I'm cancelling"
     *   - Customer: Angry! Has to wait for another provider
     */
    CANCELLATION_AFTER_ACCEPTANCE(
            0.5,
            "Provider accepted then cancelled"
    ),

    /**
     * Provider arrived late from estimated time
     * Penalty: -0.25 stars per 5 minutes late (max -1.0)
     *
     * Why: Unreliable timing, poor planning
     *
     * Examples:
     *   - Estimated: 10:00, Arrived: 10:05 → -0.25 (5 min)
     *   - Estimated: 10:00, Arrived: 10:10 → -0.5 (10 min)
     *   - Estimated: 10:00, Arrived: 10:25 → -1.0 (25 min, capped)
     */
    LATE_ARRIVAL(
            1.0,  // Max penalty (capped)
            "Provider arrived late from estimated time"
    ),

    /**
     * Provider marked service complete but it wasn't fixed
     * Penalty: -1.0 stars
     *
     * Why: Dishonest, poor quality work
     *
     * Example:
     *   - Provider: "Gas fixed!"
     *   - Customer: "No, it's still broken"
     *   - System: Apply -1.0 penalty
     */
    INCOMPLETE_SERVICE(
            1.0,
            "Provider marked complete but service incomplete"
    );

    private final double penaltyAmount;
    private final String description;

    RatingPenaltyType(double penaltyAmount, String description) {
        this.penaltyAmount = penaltyAmount;
        this.description = description;
    }

    public double getPenaltyAmount() {
        return penaltyAmount;
    }

    public String getDescription() {
        return description;
    }
}