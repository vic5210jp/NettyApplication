package com.yiqiniu.mktserver;


import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yiqiniu.common.StartServer;
import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.job.JobHelper;
import com.yiqiniu.mktserver.job.CloseWorkJob;
import com.yiqiniu.mktserver.job.HkQuotReciverJob;
import com.yiqiniu.mktserver.job.HkQuotReciverStopJob;
import com.yiqiniu.mktserver.job.OpenWorkJob;
import com.yiqiniu.mktserver.job.RefreshAdjustFactorJob;

/**
 * <code>YiQiNiuMktRTServer</code>
 *
 * @author Jimmy
 * @date 2015-7-7 下午1:53:32
 * @version v1.0
 */

public class YiQiNiuMktRTServer {
	static Logger LOG = Logger.getLogger(YiQiNiuMktRTServer.class);

	public static void main(String[] args) throws InterruptedException {
	
		new StartServer() {
			@Override
			public void execute() {
				try {
					ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
					LOG.info(" -----start run YiQiNiuMktRTServer SUCCESS ------");
					DefaultConfig defaultConfig = (DefaultConfig) ctx.getBean("defaultConfig");
					
					String openExpre = defaultConfig.getVal("open.work.expre");//0 10 09 ? * *
					String closeExpre = defaultConfig.getVal("close.work.expre");//0 05 18 ? * *
					String refreshExpre = defaultConfig.getVal("open.refresh.expre");//0 15 09 ? * *
					String synchExpre = defaultConfig.getVal("synch.cache.expre");//0 15 09 ? * *
					
					
					//港股行情接收服务器启动
					JobHelper.startJob(HkQuotReciverJob.class, defaultConfig.getVal("start.work.expre"));
					//港股行情接收服务器停止
					JobHelper.startJob(HkQuotReciverStopJob.class, defaultConfig.getVal("stop.work.expre"));
					
					JobHelper.startJob(OpenWorkJob.class, openExpre);
					JobHelper.startJob(CloseWorkJob.class, closeExpre);
					JobHelper.startJob(RefreshAdjustFactorJob.class, refreshExpre);
					//JobHelper.startJob(SynchCacheJob.class, synchExpre);
					LOG.info(" -----end run YiQiNiuMktRTServer SUCCESS---- ");
				} catch (Exception e) {
					LOG.error(" YiQiNiuMktRTServer ERROR ", e);
				}
			}
		}.start();
		
		
	}

}
