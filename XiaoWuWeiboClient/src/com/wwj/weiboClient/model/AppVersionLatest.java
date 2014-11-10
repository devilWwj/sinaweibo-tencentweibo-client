package com.wwj.weiboClient.model;

public class AppVersionLatest {
	private long fileSize;
	private boolean forceUpdate;
	private String fileVersion;
	private String updateInfo;
	private String downloadUrl;

	public AppVersionLatest() {
		super();
	}

	public AppVersionLatest(long fileSize, boolean forceUpdate,
			String fileVersion, String updateInfo, String downloadUrl) {
		super();
		this.fileSize = fileSize;
		this.forceUpdate = forceUpdate;
		this.fileVersion = fileVersion;
		this.updateInfo = updateInfo;
		this.downloadUrl = downloadUrl;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	public String getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}

	public String getUpdateInfo() {
		return updateInfo;
	}

	public void setUpdateInfo(String updateInfo) {
		this.updateInfo = updateInfo;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	@Override
	public String toString() {
		return "AppVersionLatest [fileSize=" + fileSize + ", forceUpdate="
				+ forceUpdate + ", fileVersion=" + fileVersion
				+ ", updateInfo=" + updateInfo + ", downloadUrl=" + downloadUrl
				+ "]";
	}
}
