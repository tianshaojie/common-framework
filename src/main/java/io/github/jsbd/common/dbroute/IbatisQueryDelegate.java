package io.github.jsbd.common.dbroute;

import io.github.jsbd.common.dbroute.config.DBRoute;
import io.github.jsbd.common.dbroute.config.DBRouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IbatisQueryDelegate implements QueryDelegate {

    private static final Logger logger = LoggerFactory.getLogger(IbatisQueryDelegate.class);
    private DBRouteConfig dbRouteConfig;

    @Override
    public Integer queryForCount(String countStatement, Object param, DBRoute dbRoute) {
        Map<String, SqlMapClientTemplate> dbMap = dbRouteConfig.getSqlMapTemplates(dbRoute, countStatement);
        int totalCount = 0;
        for (Map.Entry<String, SqlMapClientTemplate> e : dbMap.entrySet()) {
            String dbName = e.getKey();
            SqlMapClientTemplate st = dbMap.get(dbName);
            long startTime = System.currentTimeMillis();
            Object returnObject = st.queryForObject(countStatement, param);
            long endTime = System.currentTimeMillis();
            logRunTime(countStatement, dbName, endTime - startTime);
            totalCount += ((Integer) returnObject).intValue();
        }
        return totalCount;
    }

    @Override
    public Integer queryForCount(String countStatement, DBRoute dbRoute) {
        return queryForCount(countStatement, null, dbRoute);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryForList(String statementName, DBRoute dbRoute) {
        return queryForList(statementName, null, dbRoute);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List queryForList(String statementName, Object parameterObject, DBRoute dbRoute) {
        Map<String, SqlMapClientTemplate> dbMap = dbRouteConfig.getSqlMapTemplates(dbRoute, statementName);
        List<Object> resultList = new ArrayList<Object>();
        for (Map.Entry<String, SqlMapClientTemplate> e : dbMap.entrySet()) {
            String dbName = e.getKey();
            SqlMapClientTemplate st = dbMap.get(dbName);
            long startTime = System.currentTimeMillis();
            List list = null;
            if (parameterObject != null) {
                list = st.queryForList(statementName, parameterObject);
            } else {
                list = st.queryForList(statementName);
            }

            long endTime = System.currentTimeMillis();
            logRunTime(statementName, dbName, endTime - startTime);
            setDBRoute(dbName, list);
            resultList.addAll(list);
        }
        return resultList;
    }

    @Override
    public Object queryForObject(String statementName, Object parameterObject, DBRoute dbRoute) {
        Map<String, SqlMapClientTemplate> dbMap = dbRouteConfig.getSqlMapTemplates(dbRoute, statementName);
        for (Map.Entry<String, SqlMapClientTemplate> e : dbMap.entrySet()) {
            String dbName = e.getKey();
            SqlMapClientTemplate st = dbMap.get(dbName);
            long startTime = System.currentTimeMillis();
            Object returnObject;
            if (parameterObject != null) {
                returnObject = st.queryForObject(statementName, parameterObject);
            } else {
                returnObject = st.queryForObject(statementName);
            }
            long endTime = System.currentTimeMillis();
            logRunTime(statementName, dbName, endTime - startTime);
            if (returnObject != null) {
                setDBRoute(dbName, returnObject);
                return returnObject;
            }
        }
        return null;
    }

    @Override
    public Object queryForObject(String statementName, DBRoute dbRoute) {
        return queryForObject(statementName, null, dbRoute);
    }

    @Override
    public void setDbRouteConfig(DBRouteConfig dbRouteConfig) {
        this.dbRouteConfig = dbRouteConfig;
    }

    private void setDBRoute(String dbName, Object o) {
        if ((o != null) && io.github.jsbd.common.dbroute.BaseDO.class.isAssignableFrom(o.getClass())) {
            ((io.github.jsbd.common.dbroute.BaseDO) o).setDbRoute(new DBRoute(dbName));
        }
    }

    private void setDBRoute(String dbName, List<Object> list) {
        if (list != null) {
            for (Object o : list) {
                setDBRoute(dbName, o);
            }
        }
    }

    private void logRunTime(String statementName, String dbName, long runTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sql " + statementName + " executed on " + dbName + " databases. Run time estimated: " + runTime + "ms");
        }
    }

}
