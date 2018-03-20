package com.yiqiniu.mktserver.job;

import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.job.BaseJob;
import com.yiqiniu.mktserver.service.SynchCacheService;

/**
 * <code>SynchCacheJob<code> 同步缓存
 * 
 * @author sunstar
 * @since Yiqiniu v0.0.1 (2014-12-26)
 * 
 */
@Component
public class SynchCacheJob extends BaseJob {
	static Logger L = LoggerFactory.getLogger(SynchCacheJob.class);

	/** 锁定任务， 避免任务触发过多 */
	private volatile ReentrantLock lock = new ReentrantLock();

	@Resource
	public SynchCacheService synchCacheService;
	@Resource
	private DefaultConfig defConfig;


	@Override
	public void execute() {
		try {
			lock.lock();
			synchCacheService.synchCache();
		} catch (Exception e) {
			L.error("同步缓存作业异常", e);
		} finally {
			lock.unlock();
		}
	}

}
