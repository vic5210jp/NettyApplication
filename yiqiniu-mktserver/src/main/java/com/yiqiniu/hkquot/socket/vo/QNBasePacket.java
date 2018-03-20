package com.yiqiniu.hkquot.socket.vo;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

import com.yiqiniu.common.utils.JSONUtil;

/**
 * <code>QNPacket<code>齐牛基础数据包
 * 
 * @author Jimmy
 * @since RtpushzServer v1.0.0(June 28, 2015)
 * 
 */
public abstract class QNBasePacket<H extends QNBasePacket.QNBaseHeader, T> implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 包头 **/
	private H header;
	/** 包体 **/
	private T content;

	/**
	 * 把字节流转换成包实体
	 * @param byteBuf
	 */
	public abstract void parseFrom(ByteBuf byteBuf);
	
	/**
	 * 把包头以及包的内容写入到ByteBuf
	 * @param byteBuf
	 */
	//public abstract void writeTo(ByteBuf byteBuf);
	
	/**
	 * 包头 + 包体的长度
	 * @return
	 */
	//public abstract long getLength();

	/**
	 * 包头的长度
	 * @return
	 */
	public abstract int getHeaderLength();
	
	/**
	 * 包体的长度
	 * @return
	 */
	public abstract int getContentLength(ByteBuf byteBuf);

	public H getHeader() {
		return header;
	}

	public void setHeader(H header) {
		this.header = header;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return JSONUtil.toJson(this);
	}

	public static class QNBaseHeader {
		/*private Short msgLength;
		private Short msgType;
		private Integer seqNum;
		private Integer internalSeqNum;
		private Long sendTime;*/
		private int msgLength;
		private int msgType;
		private long seqNum;
		private long internalSeqNum;
		private long sendTime;
		public int getMsgLength() {
			return msgLength;
		}
		public void setMsgLength(int msgLength) {
			this.msgLength = msgLength;
		}
		public int getMsgType() {
			return msgType;
		}
		public void setMsgType(int msgType) {
			this.msgType = msgType;
		}
		public long getSeqNum() {
			return seqNum;
		}
		public void setSeqNum(long seqNum) {
			this.seqNum = seqNum;
		}
		public long getInternalSeqNum() {
			return internalSeqNum;
		}
		public void setInternalSeqNum(long internalSeqNum) {
			this.internalSeqNum = internalSeqNum;
		}
		public long getSendTime() {
			return sendTime;
		}
		public void setSendTime(long sendTime) {
			this.sendTime = sendTime;
		}
		
		
		
	}
}
