/**
 * @Title: MktControlService.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.control.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.common.utils.DateUtils;
import com.yiqiniu.common.utils.DateUtils.TimeFormatter;
import com.yiqiniu.mktinfo.persist.po.StkTrdCale;
import com.yiqiniu.mktinfo.persist.po.SysJobLog;
import com.yiqiniu.mktserver.control.MktControlService;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.dao.TrdDayInfoDao;
import com.yiqiniu.mktserver.service.JobLogService;
import com.yiqiniu.mktserver.service.MktJobService;
import com.yiqiniu.mktserver.util.Constants;
import com.yiqiniu.mktserver.util.LocalCache;

/**
 * <code>MktControlServiceImpl</code>
 * 
 * @author Jimmy
 * @date 2015-7-3 下午7:28:55
 * @version v1.0
 */
@Service
public class MktControlServiceImpl extends MktInfoBeanRepository implements MktControlService {
	final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private boolean isJobLogRecorded = false;
	
	@Resource
	protected JobLogService jobLogService;
	@Resource
	protected MktJobService mktJobService;
	@Resource
	protected TrdDayInfoDao trdDayInfoDao;

	@Override
	public void recordJobLog(long msgTime) {
		SysJobLog sysJobLog = null;
		Date date = new Date();
		String time = DateUtils.dateToString(date, Constants.HH_MM_00);
		String tdDate = DateUtils.dateToString(date, DateUtils.TimeFormatter.YYYY_MM_DD);
		try {
			String regionCode = defaultConfig.getVal("region.code");
			StkTrdCale stkTrdCale = LocalCache.STK_TRD_CAL_MAP.get(tdDate);
			if (stkTrdCale == null) {
				stkTrdCale = trdDayInfoDao.findTrdByNormalDay(tdDate, regionCode);
				if (stkTrdCale != null) {
					LocalCache.STK_TRD_CAL_MAP.put(tdDate, stkTrdCale);
				}
			}
			if (stkTrdCale == null) {
				LOG.error("获取交易日为空");
				return;
			}
			// 判断当天是否为交易日
			if (!stkTrdCale.getIsTradeDay()) {
				LOG.info("港交所今天非交易日");
				return;
			}
			// 不在配置的指定的时间内
			if (!isInRtTimeArea(time)) {
				return;
			}

			// 记录一天中第一次行情调度任务的状态
			if (!isJobLogRecorded) {
				sysJobLog = jobLogService.findSysJobLog(Constants.MODULE_NAME, Constants.HK_RT_JOB_NAME, tdDate);
				if (sysJobLog == null) {
					sysJobLog = new SysJobLog();
					// 任务的状态
					sysJobLog.setJobName(Constants.HK_RT_JOB_NAME);
					sysJobLog.setSysName(Constants.MODULE_NAME);
					jobLogService.saveJob(sysJobLog);
				}

				String sDate = DateUtils.dateToString(date, TimeFormatter.YYYYMMDD);
				// 消息的日期要与系统的日期相同才能进行更新状态
				if (sDate.equals(DateUtils.dateToString(msgTime, TimeFormatter.YYYYMMDD))) {
					isJobLogRecorded = true;
					// 记录当天第一次行情任务的状态
					if (sysJobLog != null) {
						sysJobLog.setEndTime(date);
						jobLogService.updateJob(sysJobLog);
					}
				}
			}
			LOG.info("HK实时行情任务记录完毕");
		} catch (Exception e) {
			LOG.error("行情任务记录异常", e);
			if (!isJobLogRecorded && sysJobLog != null) {
				jobLogService.updateError(sysJobLog, e);
			}
		}
	}
	

	@Override
	public boolean getJobLogRecordFlag() {
		return isJobLogRecorded;
	}


	@Override
	public void resetJobLogRecordFlag() {
		this.isJobLogRecorded = false;
	}


	/**
	 * 判断实时行情是否在指定的时间内
	 * 
	 * @param sTime
	 * @return
	 */
	private boolean isInRtTimeArea(String sTime) {
		String rtTimeArea = defaultConfig.getVal("rt.timeArea");
		String[] aRtTimeArea = rtTimeArea.split("\\|");
		return aRtTimeArea[0].compareTo(sTime) <= 0 && aRtTimeArea[1].compareTo(sTime) >= 0;
	}
}
