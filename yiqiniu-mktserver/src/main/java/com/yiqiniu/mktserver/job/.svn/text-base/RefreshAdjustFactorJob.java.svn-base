package com.yiqiniu.mktserver.job;

import static com.yiqiniu.mktserver.util.Constants.HK_RT_JOB_NAME;
import static com.yiqiniu.mktserver.util.Constants.MODULE_NAME;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.job.BaseJob;
import com.yiqiniu.mktinfo.persist.po.StkTrdCale;
import com.yiqiniu.mktinfo.persist.po.SysJobLog;
import com.yiqiniu.mktserver.dao.TrdDayInfoDao;
import com.yiqiniu.mktserver.service.JobLogService;
import com.yiqiniu.mktserver.service.RefreshAdjustFactorService;
import com.yiqiniu.protocol.StaticType.JobStatus;

/**
 * @description 刷新复权调整因子任务
 *
 * @author 余俊斌
 * @date 2015年7月30日 下午3:14:14
 * @version v1.0
 */
@Component
public class RefreshAdjustFactorJob extends BaseJob {
	private Logger LOG = LoggerFactory.getLogger(RefreshAdjustFactorJob.class);
	
	private CountDownLatch dependencyLatch;
	private int retryTime;
	
//	@Resource
//	private ZKMaster zkMaster;
	@Resource
	private RefreshAdjustFactorService refreshService;
	@Resource
	private JobLogService jobLogService;
	@Resource
	private TrdDayInfoDao trdDayInfoDao;
	@Resource
	private DefaultConfig defConfig;

	private static final String REFRESH_AF = "RefreshAFJob";

	@PostConstruct
	protected void preInit() {
		final String sJobStartTime = defConfig.getVal("open.refresh.time");
		retryTime = defConfig.getInt("open.refresh.retryTime");
		Executors.newSingleThreadExecutor()
		.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					if (!"1".equals(defConfig.getVal("relacunchJob").trim())) {
						return;
					}
					String sTimeNow = DateUtils.dateToString(new Date(), "HH:mm:ss");
					if (sTimeNow.compareTo(sJobStartTime) > 0) {
						refreshService.refreshStkAF(sTimeNow);
						refreshService.refreshForwardAdjust(sTimeNow);
					}
				} catch (Exception e) {
					LOG.error("启动时刷新除权因子异常", e);
				}
			}
		});
	}
	
	@Override
	public void execute() {
		try {
			Date jobBeginTime = new Date();
			String strTdDate = DateUtils.dateToString(jobBeginTime, DateUtils.TimeFormatter.YYYY_MM_DD);
			
			String regionCode = defConfig.getVal("region.code");
			StkTrdCale trdCal = trdDayInfoDao.findTrdByNormalDay(strTdDate, regionCode);
			if (!trdCal.getIsTradeDay()) {
				LOG.info("今天不是交易日，不进行" + REFRESH_AF + " 的任务");
				return;
			}
			
			SysJobLog sysJobLog = jobLogService.findSysJobLog(MODULE_NAME, REFRESH_AF, strTdDate);
			if (sysJobLog != null) {
				LOG.error("不进行" + REFRESH_AF + " 的任务, 目前的状态为:" + sysJobLog.getJobStatus());
				return;
			}
			
			dependencyLatch = new CountDownLatch(1);
			new CheckDependencyThread(retryTime, strTdDate).start();
			
			try {
				// 有限时间等待
				boolean needToGo = dependencyLatch.await(30 * retryTime + 1, TimeUnit.SECONDS);
				if (!needToGo) {
					LOG.error("开盘刷新前复权数据任务完成超时");
					return;
				}
			} catch (InterruptedException iex) {
				LOG.error("开盘刷新前复权数据任务等待依赖任务完成时出错");
				return;
			}
			
			sysJobLog = new SysJobLog();
			sysJobLog.setSysName(MODULE_NAME);
			sysJobLog.setJobName(REFRESH_AF);
			
			long start = System.currentTimeMillis();
			LOG.info(">>> start " + REFRESH_AF + " -- :" + DateUtils.dateToString(start, DateUtils.TimeFormatter.YYYY_MM_DD_HH_MM_SS));
			try {
				jobLogService.saveJob(sysJobLog);
				
				refreshService.refreshStkAF(strTdDate);
				refreshService.refreshForwardAdjust(strTdDate);
				
				jobLogService.updateJob(sysJobLog);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				jobLogService.updateError(sysJobLog, e);
			}
			
			long end = System.currentTimeMillis();
			LOG.info("---End " + REFRESH_AF + " :" + DateUtils.dateToString(start, DateUtils.TimeFormatter.YYYY_MM_DD_HH_MM_SS) + "--use time:" + (end - start) + "---");
//			if (zkMaster.isMaster()) {
//			} else {
//				LOG.info("从机不执行RefreshAFJob任务");
//			}
		} catch (Exception e) {
			LOG.error("刷新复权调整因子任务异常", e);
		} 
	}
	
	/**
	 * @description 有限等待依赖任务完成线程
	 *
	 * @author 余俊斌
	 * @date 2015年4月13日 下午2:07:59
	 * @version v1.0
	 */
	private class CheckDependencyThread extends Thread{
		/**
		 * 重试次数
		 */
		private int retryTimes;
		/**
		 * 任务时间
		 */
		private String jobDate;
		
		public CheckDependencyThread(int retryTimes, String jobDate) {
			super();
			this.retryTimes = retryTimes;
			this.jobDate = jobDate;
		}

		@Override
		public void run() {
			while (retryTimes-- > 0) {
				boolean allDependencyDone = true;
				SysJobLog dependency = null;
				
				// 依赖任务未开始或未正常结束则不执行
				// 此任务依赖行情任务
				dependency = jobLogService.findSysJobLog(MODULE_NAME, HK_RT_JOB_NAME, jobDate);
				if (dependency == null || !JobStatus.Y.name().equals(dependency.getJobStatus())) {
					LOG.info("不进行" + REFRESH_AF + " 的任务, 依赖的" + HK_RT_JOB_NAME + "任务尚未结束");
					allDependencyDone = false;
				}

				if (allDependencyDone) {
					// 如果全部依赖完成，则让等待的主线程恢复
					dependencyLatch.countDown();
					break;
				} else {
					// 如果有依赖未完成，则休眠30秒再重试
					try {
						LOG.info("刷新前复权行情任务未完成，等待30秒，剩余等待{}次", retryTimes + 1);
						TimeUnit.SECONDS.sleep(30);
					} catch (InterruptedException e) {
					}
				}
			}
			
		}

	}
}
