package org.woheller69.weather;

import java.util.Objects;

public class MethodStat {
    Integer id;
    Long startCount;
    Long endCount;

    public MethodStat(Integer id, Long startCount, Long endCount) {
        this.id = id;
        this.startCount = startCount;
        this.endCount = endCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getStartCount() {
        return startCount;
    }

    public void setStartCount(Long startCount) {
        this.startCount = startCount;
    }

    public Long getEndCount() {
        return endCount;
    }

    public void setEndCount(Long endCount) {
        this.endCount = endCount;
    }

    @Override
    public String toString() {
        return "MethodStat{" +
                "id=" + id +
                ", startCount=" + startCount +
                ", endCount=" + endCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        MethodStat that = (MethodStat) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(startCount, that.startCount) &&
                Objects.equals(endCount, that.endCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startCount, endCount);
    }
}
