package io.github.jsbd.common.dbroute;

import io.github.jsbd.common.dbroute.config.DBRoute;
import io.github.jsbd.common.dbroute.config.DBRouteConfig;

import java.util.List;

public class BaseDAO {

    private QueryDelegate queryDelegate = new IbatisQueryDelegate();
    private EntityDelegate entityDelegate = new IbatisEntityDelegate();
    private DBRoute defaultDB;

    public QueryDelegate getQueryDelegate() {
        return queryDelegate;
    }

    public void setQueryDelegate(QueryDelegate queryDelegate) {
        this.queryDelegate = queryDelegate;
    }

    public EntityDelegate getEntityDelegate() {
        return entityDelegate;
    }

    public void setEntityDelegate(EntityDelegate entityDelegate) {
        this.entityDelegate = entityDelegate;
    }

    public DBRoute getDefaultDB() {
        return defaultDB;
    }

    public void setDefaultDB(DBRoute defaultDB) {
        this.defaultDB = defaultDB;
    }

    public void setDbRouteConfig(DBRouteConfig dbRouteConfig) {
        this.queryDelegate.setDbRouteConfig(dbRouteConfig);
        this.entityDelegate.setDbRouteConfig(dbRouteConfig);
    }

    public Object insert(String statementName, Object parameterObject) {
        return getEntityDelegate().insert(statementName, parameterObject, getDefaultDB());
    }

    public int update(String statementName, Object parameterObject) {
        return getEntityDelegate().update(statementName, parameterObject, getDefaultDB());
    }

    public int delete(String statementName, Object parameterObject) {
        return getEntityDelegate().delete(statementName, parameterObject, getDefaultDB());
    }

    @SuppressWarnings("rawtypes")
    public void batchInsert(String statementName, List memberList) {
        getEntityDelegate().batchInsert(statementName, memberList, getDefaultDB());
    }

    @SuppressWarnings("rawtypes")
    public void batchUpdate(String statementName, List memberList) {
        getEntityDelegate().batchUpdate(statementName, memberList, getDefaultDB());
    }

    public Object queryForObject(String statementName, Object parameterObject) {
        return getQueryDelegate().queryForObject(statementName, parameterObject, getDefaultDB());
    }

    public Object queryForObject(String statementName) {
        return getQueryDelegate().queryForObject(statementName, getDefaultDB());
    }

    @SuppressWarnings("rawtypes")
    public List queryForList(String statementName) {
        return getQueryDelegate().queryForList(statementName, getDefaultDB());
    }

    @SuppressWarnings("rawtypes")
    public List queryForList(String statementName, Object parameterObject) {
        return getQueryDelegate().queryForList(statementName, parameterObject, getDefaultDB());
    }

    public Integer queryForCount(String countStatement, Object param) {
        return getQueryDelegate().queryForCount(countStatement, param, getDefaultDB());
    }

    public Integer queryForCount(String countStatement) {
        return getQueryDelegate().queryForCount(countStatement, getDefaultDB());
    }

}
