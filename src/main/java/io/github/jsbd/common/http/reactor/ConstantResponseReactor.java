package io.github.jsbd.common.http.reactor;

import io.github.jsbd.common.http.response.DefaultHttpResponseSender;
import io.github.jsbd.common.http.response.HttpResponseSender;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class ConstantResponseReactor implements HttpReactor {

  private HttpReactor        nextReactor    = null;
  private HttpResponse       response;
  private HttpResponseSender responseSender = new DefaultHttpResponseSender();

  public ConstantResponseReactor(HttpResponse response) {
    this.response = response;
  }

  public HttpResponse getResponse() {
    return response;
  }

  public HttpResponseSender getResponseSender() {
    return responseSender;
  }

  public void setResponseSender(HttpResponseSender responseSender) {
    this.responseSender = responseSender;
  }

  public HttpReactor getNextReactor() {
    return nextReactor;
  }

  public void setNextReactor(HttpReactor nextReactor) {
    this.nextReactor = nextReactor;
  }

  public void onHttpRequest(Channel channel, HttpRequest request) {

    String uuid = request.headers().get("uuid");
    if (uuid != null) {
      response.headers().set("uuid", uuid);
    }
    responseSender.sendResponse(channel, response);

    if (null != this.nextReactor) {
      nextReactor.onHttpRequest(null, request);
    }
  }

}
