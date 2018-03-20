package com.yiqiniu.mktserver.job;


import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.job.BaseJob;
import com.yiqiniu.mktserver.service.IReciverClient;


/**
 * <code>HKQuotReciverJob<code> 接收港股行情作业
 * 
 * @author xiongpan
 * @since Yiqiniu v0.0.1 (2014-12-26)
 * 
 */
@Component
public class HkQuotReciverStopJob extends BaseJob {
	static Logger L = LoggerFactory.getLogger(HkQuotReciverStopJob.class);

	/** 锁定任务， 避免任务触发过多 */
	private volatile ReentrantLock lock = new ReentrantLock();

	@Resource
	public IReciverClient ireciverClient;
	
	@Resource
	private DefaultConfig defConfig;

	@Override
	public void execute() {
		try {
			lock.lock();
			L.error("IReciverClient Stop--------------------------------"+ireciverClient);
			ireciverClient.stop();
		} catch (Exception e) {
			L.error("IReciverClient Stop异常...", e);
		} finally {
			lock.unlock();
		}
	}

}
