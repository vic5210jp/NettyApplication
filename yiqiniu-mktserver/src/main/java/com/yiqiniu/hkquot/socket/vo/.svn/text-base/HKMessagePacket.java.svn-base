
package com.yiqiniu.hkquot.socket.vo;

import io.netty.buffer.ByteBuf;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.api.mktserver.HkQuotMsg;
import com.yiqiniu.hkquot.util.HKquotUtil;
import com.yiqiniu.mktserver.service.MktServerService;


/**
 * <code>HKMessagePacket<code>港股推送过来的消息包  ，消息包分为: |消息头|消息体|
 * <p>
 * 消息头的组成: <br>
 * Msg length - 2个字节<br> (消息总长度，包括消息头和消息体)  
 * filter - 2个字节<br>     ()
 * seqNum - 4个字节<br>     (序列号)
 * internalseqnum - 4个字节<br>   (内部序列号)
 * sendTime - 8个字节<br>   (时间  1970年到现在的纳秒数)
 * @author xiongpan
 * 
 */
@Component
public class HKMessagePacket extends QNBasePacket<HKMessagePacket.HKMsgHeader, String> {

	@Resource
	private MktServerService mktServer;
	
	private static final long serialVersionUID = 1L;
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	/** 包头占字节的长度: 20个字节 **/
	public static final int HEADER_LENGTH = 20;
	/** 编码方式 **/
	public static final String ENCODING = "UTF-8";
	
	@Override
	public void parseFrom(ByteBuf byteBuf) {
		int iReadablLen = byteBuf.readableBytes();
		if (iReadablLen < getHeaderLength()) {
			throw new RuntimeException("目前可读的长度为:" + iReadablLen + ",包头的长度至少为:"+ getHeaderLength());
		}
		
		int iOldReaderIndex = byteBuf.readerIndex();
		
		HKMsgHeader header = new HKMsgHeader();
		int msgLength = byteBuf.readUnsignedShort();
//LOG.info("===消息总长度==="+msgLength);
		header.setMsgLength(msgLength);
		
		int msgType = byteBuf.readUnsignedShort();
		header.setMsgType(msgType);
//LOG.info("===消息类型==="+msgType);	

		long seqNum = byteBuf.readUnsignedInt();
		header.setSeqNum(seqNum);
//LOG.info("===序列号==="+seqNum);
		
		long internalSeqNum = byteBuf.readUnsignedInt();//内部序列号
//LOG.info("===内部序列号==="+internalSeqNum);
		header.setInternalSeqNum(internalSeqNum);
		
		long  sendTime = byteBuf.readLong();
		header.setSendTime(sendTime);
//LOG.info("message序列号="+seqNum+",message时间=="+sendTime+" " +new java.util.Date(sendTime/1000000));
		this.setHeader(header);int seqNum1 = (int) seqNum;
		
/******************写文件**********************/
		byte[] abData = null;
		if (msgLength > 20) {
			// byteBuf.readerIndex(byteBuf.readerIndex() + 2);
			abData = new byte[msgLength - 20];
			byteBuf.readBytes(abData);
			// byteBuf.get(abData);
			/*
			 * try { String sContent = new String(abData, ENCODING);
			 * this.setContent(sContent); } catch (UnsupportedEncodingException
			 * e) { LOG.error("不支持的编码方式:" + ENCODING, e); }
			 */
		}
		
		//转码消息e
		HkQuotMsg msg = new HkQuotMsg();
		msg.setBody(abData);
		msg.setSendTime(sendTime);

		try {
			mktServer.processReceivedQuotMsg(msg);
		} catch (Exception e) {
			LOG.error("",e);
			LOG.error("seqNum为{}sendTime为{}的message处理异常....",seqNum,sendTime);
		}
//		/HKquotUtil.writeFile_(byteBuf, seqNum,msgLength);
	//HKquotUtil.writeFile1_(seqNum1,sendTime);//写12字节的文件
		
			//byteBuf.readerIndex(iOldReaderIndex);//还原指针到消息头前面的位置
			//HKquotUtil.writeFile(byteBuf, seqNum,msgLength);
		//}else{
			//是心跳包 do nothing
			//byteBuf.readerIndex(iOldReaderIndex);//还原指针到消息头前面的位置
			//HKquotUtil.writeFile(byteBuf, seqNum,msgLength);
		//}
/******************写文件**********************/
		
	}
	

	
	// 包头的字节长度
	@Override
	public int getHeaderLength() {
		return HEADER_LENGTH;
	}
	
	

	// 消息头和消息体的长度之和
	@Override
	public int getContentLength(ByteBuf byteBuf) {
		int iOldReaderIndex = byteBuf.readerIndex();
		//byteBuf.readerIndex(4);// 包体的长度是从第四个字节开始
		int readInt = byteBuf.readUnsignedShort();
		byteBuf.readerIndex(iOldReaderIndex);
//LOG.info("消息头消息体总长度readInt================="+readInt);
		return readInt;
		//return readInt-HEADER_LENGTH;
	}

	/**
	 * <code>HKMsgHeader</code> 港股消息头
	 */
	public static class HKMsgHeader extends com.yiqiniu.hkquot.socket.vo.QNBasePacket.QNBaseHeader {
		private int msgLength;

		public int getMsgLength() {
			return msgLength;
		}

		public void setMsgLength(int msgLength2) {
			this.msgLength = msgLength2;
		}

	}
	
	
	/* public void trans(HkQuotMsg hkQuotMsg) {
		try {
			LOG.error("mktServer===" + mktServer);
			mktServer.processReceivedQuotMsg(hkQuotMsg);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			LOG.error("seqNum为{}sendTime为{}的message处理异常....",
					hkQuotMsg.getSeqNum(), hkQuotMsg.getSendTime());
		}
	}*/
}
