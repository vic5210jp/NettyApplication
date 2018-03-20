/**
 * @Title: StkHkPersistContainer.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.vo;

import java.util.List;
import java.util.Map;

import com.yiqiniu.mktinfo.persist.po.StkDayHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkWeekHk;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkYearHk;
import com.yiqiniu.mktinfo.persist.po.StkYearHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkYearHkFwd;

/**
 * @description
 *
 * @author 余俊斌
 * @date 2015年8月21日 下午5:12:46
 * @version v1.0
 */

public class StkHkPersistContainer {

	private List<StkDayHk> dayContainer;
	private StkBasePersistContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> weekContainer;
	private StkBasePersistContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> monthContainer;
	private StkBasePersistContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> yearContainer;
	private Map<String, StkHkQuotContainer> quotContainer;

	public List<StkDayHk> getDayContainer() {
		return dayContainer;
	}

	public void setDayContainer(List<StkDayHk> dayContainer) {
		this.dayContainer = dayContainer;
	}

	public StkBasePersistContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> getWeekContainer() {
		return weekContainer;
	}

	public void setWeekContainer(
			StkBasePersistContainer<StkWeekHk, StkWeekHkFwd, StkWeekHkBkw> weekContainer) {
		this.weekContainer = weekContainer;
	}

	public StkBasePersistContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> getMonthContainer() {
		return monthContainer;
	}

	public void setMonthContainer(
			StkBasePersistContainer<StkMonthHk, StkMonthHkFwd, StkMonthHkBkw> monthContainer) {
		this.monthContainer = monthContainer;
	}

	public StkBasePersistContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> getYearContainer() {
		return yearContainer;
	}

	public void setYearContainer(
			StkBasePersistContainer<StkYearHk, StkYearHkFwd, StkYearHkBkw> yearContainer) {
		this.yearContainer = yearContainer;
	}

	public Map<String, StkHkQuotContainer> getQuotContainer() {
		return quotContainer;
	}

	public void setQuotContainer(Map<String, StkHkQuotContainer> quotContainer) {
		this.quotContainer = quotContainer;
	}

}
