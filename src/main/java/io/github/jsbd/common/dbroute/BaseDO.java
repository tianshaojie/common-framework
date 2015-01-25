package io.github.jsbd.common.dbroute;

import io.github.jsbd.common.dbroute.config.DBRoute;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class BaseDO implements Serializable {

    private static final long serialVersionUID = -6931781798977896007L;
    private DBRoute dbRoute;

    public DBRoute getDbRoute() {
        return dbRoute;
    }

    public void setDbRoute(DBRoute dbRoute) {
        this.dbRoute = dbRoute;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
