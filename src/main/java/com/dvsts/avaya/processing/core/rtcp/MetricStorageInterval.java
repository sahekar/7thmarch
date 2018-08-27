package com.dvsts.avaya.processing.core.rtcp;

public class MetricStorageInterval implements Comparable<MetricStorageInterval> {
	private final String	tableName;
	private final String	detailTableName;
	private final long		startTime;
	private long			stopTime;
	private long			rowCount	= 0;

	public MetricStorageInterval(String tableName, long startTime, long stopTime) {
		this.tableName = tableName;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.detailTableName = tableName.replaceAll("CDS_", "CDSD_");
	}

	public String getTableName() {
		return tableName;
	}

	public String getDetailTableName() {
		return detailTableName;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	@Override
	public int compareTo(MetricStorageInterval that) {
		return Long.compare(this.startTime, that.startTime);
	}

	@Override
	public String toString() {
		return "MetricStorageInterval [tableName=" + tableName + ", detailTableName=" + detailTableName + ", startTime=" + startTime + ", stopTime=" + stopTime + ", rowCount=" + rowCount + "]";
	}
}
