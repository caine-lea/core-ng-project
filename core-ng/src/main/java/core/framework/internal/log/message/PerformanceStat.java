package core.framework.internal.log.message;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class PerformanceStat {
    @Property(name = "total_elapsed")
    public Long totalElapsed;
    @Property(name = "count")
    public Integer count;
    @Property(name = "read_entries")
    public Integer readEntries;
    @Property(name = "write_entries")
    public Integer writeEntries;
}
