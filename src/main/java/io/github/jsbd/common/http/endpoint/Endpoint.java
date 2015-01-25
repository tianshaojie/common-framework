package io.github.jsbd.common.http.endpoint;

import io.github.jsbd.common.lang.IpPortPair;
import io.github.jsbd.common.lang.transport.Receiver;
import io.github.jsbd.common.lang.transport.Sender;
import org.jboss.netty.channel.Channel;

import java.net.InetSocketAddress;

public interface Endpoint extends Sender, Receiver {

  void stop();
  void start();

  void setChannel(Channel channel);
  
  IpPortPair getRemoteAddress();

  public void setAddr(InetSocketAddress addr);

}
