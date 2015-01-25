package io.github.jsbd.common.dbroute;

import io.github.jsbd.common.dbroute.config.DBRoute;
import io.github.jsbd.common.dbroute.config.DBRouteConfig;

import java.util.List;

public interface QueryDelegate {

    void setDbRouteConfig(DBRouteConfig dbRouteConfig);

    Object queryForObject(String statementName, Object parameterObject, DBRoute dbRoute);

    Object queryForObject(String statementName, DBRoute dbRoute);

    @SuppressWarnings("rawtypes")
    List queryForList(String statementName, DBRoute dbRoute);

    @SuppressWarnings("rawtypes")
    List queryForList(String statementName, Object parameterObject, DBRoute dbRoute);

    Integer queryForCount(String countStatement, Object param, DBRoute dbRoute);

    Integer queryForCount(String countStatement, DBRoute dbRoute);

//  List queryForPagedList(String countStatement, String listStatement, Object param, Paginator paginator, DBRoute dbRoute);

//  List queryForPagedList(String listStatement, Object param, Paginator paginator, DBRoute dbRoute);

//  List queryForMergedList(String statementName, Object parameterObject, Paginator paginator, String orderByString, DBRoute dbRoute);

}
