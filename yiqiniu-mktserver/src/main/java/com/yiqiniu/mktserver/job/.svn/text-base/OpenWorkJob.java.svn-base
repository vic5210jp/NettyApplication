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
import com.yiqiniu.mktserver.util.Constants;

/**
 * <code>OpenWorkJob<code> 开盘作业
 * 
 * @author Colin
 * @since Yiqiniu v0.0.1 (2014-12-26)
 * 
 */
@Component
public class OpenWorkJob extends BaseJob {
	static Logger L = LoggerFactory.getLogger(OpenWorkJob.class);

	/** 锁定任务， 避免任务触发过多 */
	private volatile ReentrantLock lock = new ReentrantLock();

	@Resource
	public MktJobService mktJobService;
	@Resource
	private DefaultConfig defConfig;

	@PostConstruct
	protected void preInit() {
		
		final String sOpenTime = defConfig.getVal("open.work.time");
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (!"1".equals(defConfig.getVal("relacunchJob").trim())) {
						return;
					}
					String sTimeNow = DateUtils.dateToString(new Date(), "HH:mm:ss");
					if (sTimeNow.compareTo(sOpenTime) > 0 
							&& sTimeNow.compareTo(Constants.TS_START_TIME) < 0) {
						//9:10 --9:30
						mktJobService.openWork();
					}
				} catch (Exception e) {
					L.error("启动是进行开盘作业异常", e);
				}
			}
		});
	}

	@Override
	public void execute() {
		try {
			lock.lock();
			mktJobService.openWork();
		} catch (Exception e) {
			L.error("开盘作业异常", e);
		} finally {
			lock.unlock();
		}
	}

}
