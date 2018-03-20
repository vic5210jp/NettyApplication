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
import com.yiqiniu.mktserver.service.IReciverClient;


/**
 * <code>HKQuotReciverJob<code> 接收港股行情作业
 * 
 * @author xiongpan
 * 
 */
@Component
public class HkQuotReciverJob extends BaseJob {
	static Logger L = LoggerFactory.getLogger(HkQuotReciverJob.class);

	/** 锁定任务， 避免任务触发过多 */
	private volatile ReentrantLock lock = new ReentrantLock();

	@Resource
	public IReciverClient ireciverClient;
	
	@Resource
	private DefaultConfig defConfig;

	@PostConstruct
	protected void preInit() {
		
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					/*if (!"1".equals(defConfig.getVal("relacunchJob").trim())) {
						return;
					}*/
					String sTimeNow = DateUtils.dateToString(new Date(), "HH:mm:ss");
					if (sTimeNow.compareTo(defConfig.getVal("startReceiveTime")) > 0 
							&& sTimeNow.compareTo(defConfig.getVal("stopReceiveTime")) < 0) {
						ireciverClient.asyncStart();//启动港股接收服务
						L.error("IReciverClient init Start-------------------"+ireciverClient);
					}
					
				} catch (Exception e) {
					L.error("初始化启动IReciverClient异常", e);
				}
			}
		});
	}

	@Override
	public void execute() {
		try {
			lock.lock();
			L.error("IReciverClient Start-------------------"+ireciverClient);
			ireciverClient.asyncStart();//启动港股接收服务
		} catch (Exception e) {
			L.error("自动启动IReciverClient异常", e);
		} finally {
			lock.unlock();
		}
	}

}
