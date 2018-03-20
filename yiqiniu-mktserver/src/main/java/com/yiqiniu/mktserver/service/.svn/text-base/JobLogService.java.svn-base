package com.yiqiniu.mktserver.service;

import com.yiqiniu.mktinfo.persist.po.SysJobLog;

public interface JobLogService {
	// 存储 sysJobLog错误信息
	void saveJob(SysJobLog sysJobLog);

	// 更新sysJobLog错误信息
	void updateJob(SysJobLog sysJobLog);
	
	SysJobLog findSysJobLog(String sysName, String jobName, String jobDate);

	// 更新sysJobLog错误信息
	void updateError(SysJobLog sysJobLog, Exception e);
}
