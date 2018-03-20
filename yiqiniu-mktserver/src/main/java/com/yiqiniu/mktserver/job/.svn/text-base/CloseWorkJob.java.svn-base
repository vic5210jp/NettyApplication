package com.yiqiniu.mktserver.job;


import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.job.BaseJob;
import com.yiqiniu.mktserver.service.MktJobService;

/**
 * <code>CloseWorkJob<code> 收盘作业
 *
 * @author Colin 
 * @since  Yiqiniu v0.0.1 (2014-12-26)
 *
 */
@Component
public class CloseWorkJob extends BaseJob {
	static Logger L = LoggerFactory.getLogger(CloseWorkJob.class);
	/** 锁定任务， 避免任务触发过多 */
	private volatile ReentrantLock lock = new ReentrantLock();
	@Resource
	public MktJobService mktJobService;
//	@Resource
//	private ZKMaster zkMaster;
	@Resource
	private DefaultConfig defConfig;
	
	@PostConstruct
	protected void preInit() {
		final String sCloseTime = defConfig.getVal("close.work.time");
		Executors.newSingleThreadExecutor()
		.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					if (!"1".equals(defConfig.getVal("relacunchJob").trim())) {
						return;
					}
					String sTimeNow = DateUtils.dateToString(new Date(), "HH:mm:ss");
					if (sTimeNow.compareTo(sCloseTime) > 0) {
						mktJobService.closeWork();
					}
				} catch (Exception e) {
					L.error("启动是进行收盘作业异常", e);
				}
			}
		});
	}
	
	@Override
	public void execute() {
		long start = System.currentTimeMillis();
		L.info("--- run Sh Mkt close Work Job -- 时间:" + DateUtils.dateToString(start, DateUtils.TimeFormatter.YYYY_MM_DD_HH_MM_SS));
		try {
			lock.lock();
			mktJobService.closeWork();
//			if (zkMaster.isMaster()) {
//			} else {
//				L.info("不是Master， 不会执行收盘工作.");
//			}
		} catch (Exception e) {
			L.error("收盘作业异常", e);
		} finally {
			lock.unlock();
		}
	}

}
