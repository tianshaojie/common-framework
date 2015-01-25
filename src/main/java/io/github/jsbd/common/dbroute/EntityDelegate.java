package io.github.jsbd.common.dbroute;

import io.github.jsbd.common.dbroute.config.DBRoute;
import io.github.jsbd.common.dbroute.config.DBRouteConfig;

import java.util.List;


public interface EntityDelegate {

    void setDbRouteConfig(DBRouteConfig dbRouteConfig);

    int delete(String statementName, Object parameterObject, DBRoute dbRoute);

    int update(String statementName, Object parameterObject, DBRoute dbRoute);

    Object insert(String statementName, Object parameterObject, DBRoute dbRoute);

    @SuppressWarnings("rawtypes")
    void batchInsert(final String statementName, final List memberList, DBRoute dbRoute);

    @SuppressWarnings("rawtypes")
    void batchUpdate(final String statementName, final List memberList, DBRoute dbRoute);
}
