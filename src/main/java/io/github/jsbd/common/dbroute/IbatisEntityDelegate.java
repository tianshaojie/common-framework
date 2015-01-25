package io.github.jsbd.common.dbroute;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import io.github.jsbd.common.dbroute.config.DBRoute;
import io.github.jsbd.common.dbroute.config.DBRouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class IbatisEntityDelegate implements io.github.jsbd.common.dbroute.EntityDelegate {

    private static final Logger logger = LoggerFactory.getLogger(IbatisEntityDelegate.class);

    private DBRouteConfig dbRouteConfig;

    @Override
    public int delete(String statementName, Object parameterObject, DBRoute dbRoute) {
        Map.Entry<String, SqlMapClientTemplate> e = getSqlMapTemplate(dbRoute, statementName);

        String dbName = e.getKey();
        SqlMapClientTemplate st = e.getValue();

        long startTime = System.currentTimeMillis();
        int affectSize = st.delete(statementName, parameterObject);
        long endTime = System.currentTimeMillis();

        logRunTime(statementName, dbName, endTime - startTime);

        return affectSize;
    }

    @Override
    public int update(String statementName, Object parameterObject, DBRoute dbRoute) {
        Map.Entry<String, SqlMapClientTemplate> e = getSqlMapTemplate(dbRoute, statementName);

        String dbName = e.getKey();
        SqlMapClientTemplate st = e.getValue();
        long startTime = System.currentTimeMillis();
        int affectSize = st.update(statementName, parameterObject);
        long endTime = System.currentTimeMillis();
        logRunTime(statementName, dbName, endTime - startTime);

        return affectSize;
    }

    @Override
    public Object insert(String statementName, Object parameterObject, DBRoute dbRoute) {
        Map.Entry<String, SqlMapClientTemplate> e = getSqlMapTemplate(dbRoute, statementName);

        String dbName = e.getKey();
        SqlMapClientTemplate st = e.getValue();

        long startTime = System.currentTimeMillis();
        Object returnObject = st.insert(statementName, parameterObject);
        long endTime = System.currentTimeMillis();
        logRunTime(statementName, dbName, endTime - startTime);

        return returnObject;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void batchInsert(final String statementName, final List memberList, DBRoute dbRoute) {
        Map.Entry<String, SqlMapClientTemplate> e = getSqlMapTemplate(dbRoute, statementName);

        String dbName = e.getKey();
        SqlMapClientTemplate st = e.getValue();

        long startTime = System.currentTimeMillis();

        st.execute(new SqlMapClientCallback() {
            public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {

                executor.startBatch();
                for (Object tObject : memberList) {
                    executor.insert(statementName, tObject);
                }
                executor.executeBatch();
                return null;
            }
        });

        long endTime = System.currentTimeMillis();
        logRunTime(statementName, dbName, endTime - startTime);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void batchUpdate(final String statementName, final List memberList, DBRoute dbRoute) {
        Map.Entry<String, SqlMapClientTemplate> e = getSqlMapTemplate(dbRoute, statementName);
        String dbName = e.getKey();
        SqlMapClientTemplate st = e.getValue();

        long startTime = System.currentTimeMillis();

        st.execute(new SqlMapClientCallback() {
            public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {
                executor.startBatch();
                for (Object tObject : memberList) {
                    executor.update(statementName, tObject);
                }
                executor.executeBatch();
                return null;
            }
        });

        long endTime = System.currentTimeMillis();
        logRunTime(statementName, dbName, endTime - startTime);

    }

    protected Map.Entry<String, SqlMapClientTemplate> getSqlMapTemplate(DBRoute dbRoute, String statementName) {
        Map<String, SqlMapClientTemplate> dbMap = dbRouteConfig.getSqlMapTemplates(dbRoute, statementName);

        if (dbMap.isEmpty()) {
            throw new RuntimeException("no database found, please confirm the parameters. DBRoute=[" + dbRoute + "], statement=[" + statementName + "]");
        }

        if (dbMap.size() != 1) {
            throw new RuntimeException("more than 1 database found, please confirm the parameters. DBRoute=[" + dbRoute + "], statement=[" + statementName + "]");
        }

        return dbMap.entrySet().iterator().next();
    }

    public DBRouteConfig getDbRouteConfig() {
        return dbRouteConfig;
    }

    public void setDbRouteConfig(DBRouteConfig dbRouteConfig) {
        this.dbRouteConfig = dbRouteConfig;
    }

    private void logRunTime(String statementName, String dbName, long runTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sql " + statementName + " executed on " + dbName + " databases. Run time estimated: " + runTime + "ms");
        }
    }

}
