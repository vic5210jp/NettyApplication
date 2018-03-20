package com.yiqiniu.hkquotReciver.client.session;


/**
 * <code>ResponseHolder</code>
 *
 * @author Jimmy
 * @since Trading - v.0.0.1(April 12, 2014)
 * 
 */
public class ResponseHolder {
	private String m_sResult;
	private String m_sSeqNum;
	
	/**
	 * @param sSessionID
	 * @param sSeqNum
	 */
	public ResponseHolder(String sSeqNum) {
		this(sSeqNum, null);
	}
	/**
	 * @param sSessionID
	 * @param hReqParams
	 */
	public ResponseHolder(String sSeqNum, String sResp) {
		m_sResult = sResp;
		m_sSeqNum = sSeqNum;
	}
	/**
	 * @return
	 */
	public String result() {
		return m_sResult;
	}
	/**
	 * @return
	 */
	public void result(String sResp) {
		m_sResult = sResp;
	}
	/**
	 * @param sSeqNum
	 */
	public void seqNum(String sSeqNum) {
		m_sSeqNum = sSeqNum;
	}
	/**
	 * @return
	 */
	public String seqNum() {
		return m_sSeqNum;
	}
}
