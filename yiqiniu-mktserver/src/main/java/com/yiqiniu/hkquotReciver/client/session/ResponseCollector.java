package com.yiqiniu.hkquotReciver.client.session;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yiqiniu.hkquotReciver.client.session.ResponseHolder;
import com.yiqiniu.hkquotReciver.client.session.SessionManager;


/**
 * 响应的收集器
 */
public class ResponseCollector {
	private Logger LOG = LoggerFactory.getLogger(ResponseCollector.class);
	
	private static final long TIME_OUT = 5000;
	private ArrayBlockingQueue<String> resultQueue;
	private String seqNum;
	private SessionManager m_oMgr;
	
	/**
	 * @param sSeqNum
	 * @param oMgr
	 */
	public ResponseCollector(String sSeqNum, SessionManager oMgr) {
		seqNum = sSeqNum;
		resultQueue = new ArrayBlockingQueue<String>(1);
		m_oMgr = oMgr;
	}
	/**
	 * @return
	 */
	public String getResult() {
		try {
			return resultQueue.poll(TIME_OUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			LOG.error("响应消息队列异常", e);
		} finally {
			m_oMgr.removeRspCollector(this);
		}
		return null;
	}
	/**
	 * @param oRspHolder
	 * @return
	 * @throws InterruptedException
	 */
	public boolean accept(ResponseHolder oRspHolder) throws InterruptedException {
		if (seqNum.equals(oRspHolder.seqNum())) {
			resultQueue.offer(oRspHolder.result());
			return true;
		}
		return false;
	}
}
