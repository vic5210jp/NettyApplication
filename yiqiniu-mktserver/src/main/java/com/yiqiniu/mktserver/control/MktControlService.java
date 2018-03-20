/**
 * @Title: MktControlService.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.control;



/**
 * <code>MktControlService</code>
 *
 * @author Jimmy
 * @date 2015-7-3 下午1:49:00
 * @version v1.0
 */

public interface MktControlService {
	/**
	 * 记录任务日志
	 * @param msgTime 触发记录的时间
	 */
	void recordJobLog(long msgTime);
	
	/**
	 * 当日任务日志记录标志
	 * @return true - 已记录， false - 未记录
	 */
	boolean getJobLogRecordFlag();
	
	/**
	 * 重置当日任务日志记录标志为false
	 */
	void resetJobLogRecordFlag();
}
