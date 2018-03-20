package com.yiqiniu.mktserver.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.yiqiniu.mktinfo.persist.po.SysJobLog;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.service.JobLogService;
import com.yiqiniu.protocol.StaticType;

/**
 * <code>JobLogServiceImpl<code> 任务的状态记录类
 * 
 * @author Colin
 * @since Yiqiniu v0.0.1 (2014-12-26)
 * 
 */
@Service
public class JobLogServiceImpl extends MktInfoBeanRepository implements JobLogService {

	@Override
	public void saveJob(SysJobLog sysJobLog) {
		if (sysJobLog != null) {
			Date date = new Date();
			sysJobLog.setStartTime(date);
			sysJobLog.setJobStatus(StaticType.JobStatus.R.toString());
			sysJobLog.setJobDate(date);
			SysJobLog sysJob = jobService.saveJobLog(sysJobLog);
			sysJobLog.setJobLogId(sysJob.getJobLogId());
		}
	}

	@Override
	public void updateJob(SysJobLog sysJobLog) {
		if (sysJobLog != null) {
			sysJobLog.setJobStatus(StaticType.JobStatus.Y.toString());
			sysJobLog.setEndTime(new Date());
			jobService.updateJobLog(sysJobLog);
		}
	}
	@Override
	public SysJobLog findSysJobLog(String sysName, String jobName, String jobDate) {
		return jobService.findJobLog(sysName, jobName, jobDate);
	}
	
	@Override
	public void updateError(SysJobLog sysJobLog, Exception e) {
		if (sysJobLog != null) {
			sysJobLog.setErrMsg(e.getMessage());
			sysJobLog.setEndTime(new Date());
			sysJobLog.setJobStatus(StaticType.JobStatus.F.toString());
			jobService.updateJobLog(sysJobLog);
		}
	}
}
