package com.ecommerce.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Observer in the Observer Pattern.
 * Simulates a central business intelligence and audit logger tracking all store events.
 * 
 * @author baktiyarovamereilim-lang
 */
public class AnalyticsAuditObserver implements Observer {
    private final List<EcommerceEvent> auditLog = new ArrayList<>();

    @Override
    public String getObserverId() {
        return "AUDIT-SYSTEM-01";
    }

    @Override
    public String getObserverName() {
        return "Audit & BI Metric Observer";
    }

    @Override
    public void update(EcommerceEvent event) {
        auditLog.add(event);
        System.out.printf("[BI Audit Log]: %s%n", event);
    }

    public List<EcommerceEvent> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }

    public int countEventsByType(EventType type) {
        return (int) auditLog.stream().filter(e -> e.getType() == type).count();
    }
}
