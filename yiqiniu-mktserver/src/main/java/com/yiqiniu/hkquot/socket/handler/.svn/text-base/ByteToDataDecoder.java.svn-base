package com.yiqiniu.hkquot.socket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteOrder;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yiqiniu.api.mktserver.HkQuotMsg;


/**
 * The <code>ByteToDataDecoder</code> 是一个解码器，把Buffer里面的字节数据转换成data（可识别）
 * 
 */
//implements ApplicationContextAware
@Component
public class ByteToDataDecoder extends ByteToMessageDecoder  {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	/*	说明：接收message过程中，当发生断点续传时（客户端与服务器断开重连），
	 *  客户端发送的十二字节的此message，聚源港股服务器会重新发送回来，此message会重复一次，无影响
	 * */
	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf in,List<Object> out) throws Exception {
		int iReadableBytes = in.readableBytes();
		in = in.order(ByteOrder.LITTLE_ENDIAN);
		//HKMessagePacket hkMsgPacket = ByteToDataDecoder.applicationContext.getBean(HKMessagePacket.class);
		
		// 可读的字节小于包头的长度, 不需要解码
		if (iReadableBytes < 20) {
			return;
		}
		
		int iOldReaderIndex = in.readerIndex();
		int readInt = in.readUnsignedShort();
		in.readerIndex(iOldReaderIndex);
		// 可读的字节小于包头+包体的长度, 不需要解码
		//if (iReadableBytes < (hkMsgPacket.getContentLength(in))) {
		if (iReadableBytes < readInt) {
			return;
		}
		// 解析港股数据包
		//hkMsgPacket.parseFrom(in);
		
		HkQuotMsg hkQuotMsg = parseFromByteBuf(in );
		out.add(hkQuotMsg);
		
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// 主要是解决SLB 心跳监测关闭客户端连接而产生的异常
		// 经常出现的Connection reset by peer: 原因可能是多方面的，不过更常见的原因是：
		// 服务器的并发连接数超过了其承载量，服务器会将其中一些连接Down掉；
		// 客户关掉了浏览器，而服务器还在给客户端发送数据；
		// 浏览器端按了Stop；
		// 服务器给客户端响应结果给防火墙拦截了;
		// FIXME 此处是主动连接，不经过SLB
//		if (!(cause.getMessage().indexOf("Connection reset by peer") > -1)) {
//			LOG.error("SLB 心跳监测关闭客户端连接而产生的异常");
//		}
		LOG.error("netty caught exception!!!",cause);
		ctx.close();
	}

	/*@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ByteToDataDecoder.applicationContext = applicationContext;
	}*/

	
	//xiongpan
	public HkQuotMsg parseFromByteBuf(ByteBuf byteBuf ) {
		int iReadablLen = byteBuf.readableBytes();
		if (iReadablLen < 20) {
			throw new RuntimeException("目前可读的长度为:" + iReadablLen + ",包头的长度至少为:"+ 20);
		}
		
	//	int iOldReaderIndex = byteBuf.readerIndex();
		HkQuotMsg hkQuotMsg = new HkQuotMsg();
		
		//HkQuotMsg hkQuotMsg = new HkQuotMsg();
		int msgLength = byteBuf.readUnsignedShort();
//LOG.info("===消息总长度==="+msgLength);
		//header.setMsgLength(msgLength);
		hkQuotMsg.setMsgLength(msgLength);
		
		int msgType = byteBuf.readUnsignedShort();
		//header.setMsgType(msgType);
//LOG.info("===消息类型==="+msgType);	

		long seqNum = byteBuf.readUnsignedInt();
		//header.setSeqNum(seqNum);
		hkQuotMsg.setSeqNum(seqNum);
//LOG.info("===序列号==="+seqNum);
		//LOG.error("序列号======={}",hkQuotMsg.getSeqNum());
		long internalSeqNum = byteBuf.readUnsignedInt();//内部序列号
//LOG.info("===内部序列号==="+internalSeqNum);
		//header.setInternalSeqNum(internalSeqNum);
		hkQuotMsg.setInternalSeqNum(internalSeqNum);
		
		long  sendTime = byteBuf.readLong();
		hkQuotMsg.setSendTime(sendTime);
		
//LOG.info("message序列号="+seqNum+",message时间=="+sendTime+" " +new java.util.Date(sendTime/1000000));
		
		byte[] abData = null;
		if (msgLength > 20) {
			abData = new byte[msgLength - 20];
			byteBuf.readBytes(abData);
		}
		
		hkQuotMsg.setBody(abData);

		return hkQuotMsg;
	}
	

}
