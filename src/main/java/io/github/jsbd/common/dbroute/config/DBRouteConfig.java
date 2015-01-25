package io.github.jsbd.common.dbroute.config;

import com.ibatis.sqlmap.client.SqlMapClient;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import java.util.*;

public class DBRouteConfig {

  private List<String>                      allNodeNameList     = new ArrayList<String>();
  private List<String>                      defaultNodeNameList = new ArrayList<String>();
  private Map<String, String>               statementRuleMap    = new HashMap<String, String>();
  private List<DBRouteRule>                 dbRuleList          = new ArrayList<DBRouteRule>();
  private Map<String, SqlMapClient>         sqlMapList;
  private Map<String, SqlMapClientTemplate> sqlMapTemplateList  = new HashMap<String, SqlMapClientTemplate>();

  public void setNodeRuleMap(Map<String, Properties> nodeRuleMap) {
    if (nodeRuleMap.size() == 0) {
      return;
    }

    for (Iterator<String> it = nodeRuleMap.keySet().iterator(); it.hasNext();) {
      String dbName = (String) it.next();
      DBRouteRule dbRule = new DBRouteRule(dbName.trim());

      Properties element = (Properties) nodeRuleMap.get(dbName);
      for (Iterator<Map.Entry<Object, Object>> iter = element.entrySet().iterator(); iter.hasNext();) {
        Map.Entry<Object, Object> entry = iter.next();

        String ruleKey = (String) entry.getKey();
        String routingRule = (String) entry.getValue();

        dbRule.addRule(ruleKey, routingRule);
      }

      dbRuleList.add(dbRule);
    }
  }

  public void setStatementRuleMap(Map<String, String> statementRuleMap) {
    this.statementRuleMap = statementRuleMap;
  }

  public void setDefaultNodeNameList(List<String> defaultNodeNameList) {
    this.defaultNodeNameList = defaultNodeNameList;
  }

  public List<String> routingDB(io.github.jsbd.common.dbroute.config.DBRoute dbRoute, String statement) {
    List<String> nodeNameListByNodeRule = routingDB(dbRoute);
    if ((nodeNameListByNodeRule != null) && !nodeNameListByNodeRule.isEmpty()) {
      return nodeNameListByNodeRule;
    }
    List<String> nodeNameListByStatementRule = routingDB(statement);
    if (nodeNameListByStatementRule != null && !nodeNameListByStatementRule.isEmpty()) {
      return nodeNameListByStatementRule;
    }
    return defaultNodeNameList;
  }

  public List<String> routingDB(io.github.jsbd.common.dbroute.config.DBRoute dbRoute) {
    if (null == dbRoute) {
      return new ArrayList<String>();
    }

    List<String> nodeNameList = new ArrayList<String>();

    if (dbRoute.getRoutingStrategy() == DBRoutingStrategy.BY_XID) {
      String xid = dbRoute.getXid();

      if (xid != null) {
        if (xid.indexOf(",") != -1) {
          StringTokenizer st = new StringTokenizer(xid, ",");

          while (st.hasMoreTokens()) {
            String dbxid = st.nextToken();
            dbxid = dbxid.trim();

            if (allNodeNameList.contains(dbxid)) {
              nodeNameList.add(dbxid);
            }
          }

          return nodeNameList;
        } else if (allNodeNameList.contains(xid)) {
          nodeNameList.add(xid);
          return nodeNameList;
        }
      }

    } else if (dbRoute.getRoutingStrategy() == DBRoutingStrategy.BY_ITEM) {
      Map<String, String> items = dbRoute.getItems();
      if (items == null || items.isEmpty()) {
        return nodeNameList;
      }

      for (DBRouteRule routeRule : dbRuleList) {
        if (routeRule.isMatched(items)) {
          nodeNameList.add(routeRule.getDbName());
          return nodeNameList;
        }
      }
    }

    return nodeNameList;
  }

  public List<String> routingDB(String statement) {
    if (statement == null) {
      return new ArrayList<String>();
    }

    String xid = (String) statementRuleMap.get(statement);
    if (xid != null) {
      List<String> nodeNameList = new ArrayList<String>();
      if (xid.indexOf(",") != -1) {
        StringTokenizer st = new StringTokenizer(xid, ",");
        while (st.hasMoreTokens()) {
          String dbxid = st.nextToken();
          dbxid = dbxid.trim();
          if (allNodeNameList.contains(dbxid)) {
            nodeNameList.add(dbxid);
          }
        }
        return nodeNameList;
      } else if (allNodeNameList.contains(xid)) {
        nodeNameList.add(xid);
        return nodeNameList;
      }
    }
    return new ArrayList<String>();
  }

  public Map<String, SqlMapClientTemplate> getSqlMapTemplates(io.github.jsbd.common.dbroute.config.DBRoute dr, String sqlId) {
    List<String> dbNameList = this.routingDB(dr, sqlId);
    if (null == dbNameList || dbNameList.isEmpty()) {
      throw new RuntimeException("No database found, please confirm the parameters. DBRoute=[" + dr + "], statement=[" + sqlId + "]");
    }

    Map<String, SqlMapClientTemplate> retDbList = new HashMap<String, SqlMapClientTemplate>();
    for (int i = 0; i < dbNameList.size(); i++) {
      String dbName = (String) dbNameList.get(i);
      SqlMapClientTemplate o = sqlMapTemplateList.get(dbName);
      if (o != null) {
        retDbList.put(dbName, o);
      }
    }
    return retDbList;
  }

  public Map<String, SqlMapClient> getSqlMapList() {
    return sqlMapList;
  }

  public void setSqlMapList(Map<String, SqlMapClient> sqlMapList) {
    this.sqlMapList = sqlMapList;
    for (Iterator<String> it = sqlMapList.keySet().iterator(); it.hasNext();) {
      String dbKey = it.next();
      SqlMapClient sqlMapClient = (SqlMapClient) sqlMapList.get(dbKey);
      SqlMapClientTemplate sqlMT = new SqlMapClientTemplate();
      sqlMT.setSqlMapClient(sqlMapClient);
      sqlMapTemplateList.put(dbKey, sqlMT);
    }

    this.allNodeNameList.addAll(sqlMapList.keySet());
  }

  public Map<String, SqlMapClientTemplate> getSqlMapTemplateList() {
    return sqlMapTemplateList;
  }

  public void setSqlMapTemplateList(Map<String, SqlMapClientTemplate> sqlMapTemplateList) {
    this.sqlMapTemplateList = sqlMapTemplateList;
  }

}
