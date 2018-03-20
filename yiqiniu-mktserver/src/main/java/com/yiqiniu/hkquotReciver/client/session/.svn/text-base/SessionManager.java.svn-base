package com.yiqiniu.hkquotReciver.client.session;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.yiqiniu.core.rtpushserver.server.vo.QNServerProbuf.QNServerPacket;

/**
 * <code>ClientSessionManager</code> processes all the requests that from clients.
 *处理港股行情源过来的message
 * @author Jimmy
 * @since Trading - v.0.0.1(April 12, 2014)
 * 
 */
public class SessionManager {
	private final Logger L = LoggerFactory.getLogger(getClass());
	/** **/
	private Map<String, Channel> channelsMapping;
	/** **/
	private LinkedBlockingQueue<ResponseHolder> m_oRespQueue = new LinkedBlockingQueue<ResponseHolder>(1024 * 10);
	/** **/
	private ResponseMonitor m_monitor;
	/** **/
	private volatile boolean m_bRunning = true;
	/** **/
	private final int m_iWorkerCount = 5;
	
	private Thread[] m_oWorks;
	
	private Set<ResponseCollector> vRspCollectors;
	/**
	 */
	public SessionManager() {
		channelsMapping = new ConcurrentHashMap<String, Channel>();
		vRspCollectors = new CopyOnWriteArraySet<ResponseCollector>();
		m_monitor = new ResponseMonitor();
		m_oWorks = new Thread[m_iWorkerCount];
	}
	/**
	 * @param oRspCollector
	 * @return
	 */
	public boolean addRspCollector(ResponseCollector oRspCollector) {
		return vRspCollectors.add(oRspCollector);
	}
	/**
	 * @param oRspCollector
	 * @return
	 */
	public boolean removeRspCollector(ResponseCollector oRspCollector) {
		return vRspCollectors.remove(oRspCollector);
	}
	/**
	 * @param sSessionID
	 * @param oClientSession
	 */
	public void registerChannel(String hostAddr, Channel channel) {
		channelsMapping.put(hostAddr, channel);
	}
	/**
	 * @param sSessionID
	 */
	public void unregisterChannel(String hostAddr) {
		channelsMapping.remove(hostAddr);
	}
	/**
	 * @param sSessionID
	 */
	public Channel getClientChannel(String sHostAddr) {
		return channelsMapping.get(sHostAddr);
	}
	/**
	 * @param oReq
	 */
	/*public ResponseCollector sendRequest(String sHostAddr, QNServerPacket oReq, boolean isAsyn) {
		Channel ch = getClientChannel(sHostAddr);
		if (ch != null) {
			if (isAsyn) {
				ch.writeAndFlush(oReq);
			} else {
				ch.writeAndFlush(oReq);
				ResponseCollector oRspClt = new ResponseCollector(oReq.getId(), this);
				addRspCollector(oRspClt);
				return oRspClt;
			}
		}
		return null;
	}*/
	/**
	 * @param oResp
	 */
	/*public void processResponse(ResponseHolder oResp) {
		try {
			L.debug("processTradeResponse start");
			m_oRespQueue.put(oResp);
			L.debug("processTradeResponse end");
		} catch (InterruptedException e) {   
			e.printStackTrace();
		}
	}*/
	/**
	 * 
	 */
	public void startResponseMonitor() {
		L.info("Starting Reponse Monitor.");
		for (int i = 0; i < m_iWorkerCount; i++) {
			m_oWorks[i] = new Thread(m_monitor);
			m_oWorks[i].setName("Reponse Monitor Thread[" + i + "]");
			m_oWorks[i].start();
		}
	}
	/**
	 * 
	 */
	public void stopResponseMonitor() {
		L.info("正在停止 Response Monitor.");
		m_bRunning = false;
		// 关闭channel
		for (Entry<String, Channel> entry : channelsMapping.entrySet()) {
			entry.getValue().close();
		}
		// 处理未处理的内容
		for (int i = 0; i < m_iWorkerCount; i++) {
			try {
				m_oWorks[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 */
	private class ResponseMonitor implements Runnable{
		@Override
		public void run() {
			while (m_bRunning) {
				try {
					ResponseHolder oRespHolder = m_oRespQueue.take();
					L.debug("ResponseMonitor start");
					for (ResponseCollector oRspClt : vRspCollectors) {
						oRspClt.accept(oRespHolder);
					}
					L.debug("ResponseMonitor End");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
}
