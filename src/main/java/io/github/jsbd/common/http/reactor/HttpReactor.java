package io.github.jsbd.common.http.reactor;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;

public interface HttpReactor {

  void onHttpRequest(Channel channel, HttpRequest request);

}
