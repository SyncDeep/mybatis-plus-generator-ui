package com.github.davidfantasy.mybatisplus.generatorui.sqlparser;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.github.davidfantasy.mybatisplus.generatorui.common.ServiceException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class DynamicParamSqlEnhancer {

    private DataSourceConfig dataSourceConfig;

    public DynamicParamSqlEnhancer(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    /**
     * SQL의 Dynamic Condition 구문 분석
     *
     * @param sql
     * @return
     */
    public List<ConditionExpr> parseSqlDynamicConditions(String sql) {
        if (Strings.isNullOrEmpty(sql)) {
            throw new ServiceException("SQL문을 입력하세요.");
        }
        try {
            CCJSqlParser ccjSqlParser = new CCJSqlParser(sql);
            Statement statement = ccjSqlParser.Statement();
            if (statement instanceof Select) {
                SelectConditionParser parser = new SelectConditionParser();
                Select select = (Select) statement;
                select.getSelectBody().accept(parser);
                return parser.getParsedConditions();
            } else {
                throw new ServiceException("Select문만 처리할 수 있습니다");
            }
        } catch (Exception e) {
            log.error("SQL Condition 구문 분석중 오류", e);
            throw new ServiceException("SQL Condition 구문 분석중 오류");
        }
    }

    /**
     * SQL에 표시된 Dynamic Condition을 Mybatis Dynamic SQL로 변환
     */
    public String enhanceDynamicConditions(String sql) {
        List<ConditionExpr> conditions = parseSqlDynamicConditions(sql);
        for (ConditionExpr condition : conditions) {
            Pattern pattern = Pattern.compile(condition.getFindPattern(), Pattern.CASE_INSENSITIVE);
            sql = ReUtil.replaceAll(sql, pattern, toDynamicSql(condition));
        }
        return sql;
    }

    /**
     * SQL in query 처리
     */
    public String clearIllegalStatements(String sql) {
        List<ConditionExpr> conditions = parseSqlDynamicConditions(sql);
        for (ConditionExpr condition : conditions) {
            if ("in".equalsIgnoreCase(condition.getOperator())) {
                Pattern pattern = Pattern.compile(condition.getFindPattern(), Pattern.CASE_INSENSITIVE);
                String replaceStr = " " + condition.getLogicOperator() + " " + condition.getLeftExpr() + " in ('')";
                sql = ReUtil.replaceAll(sql, pattern, replaceStr);
            }
        }
        return sql;
    }

    public String toDynamicSql(ConditionExpr condition) {
        StringBuilder tmp = new StringBuilder();
        if (Strings.isNullOrEmpty(condition.getLogicOperator())) {
            tmp.append(" 1=1 ");
        }
        tmp.append("\n<if test=\"");
        tmp.append(trimQuotes(condition.getParamNames().get(0)));
        tmp.append("!=null\">");
        tmp.append("\n ");
        if (!Strings.isNullOrEmpty(condition.getLogicOperator())) {
            tmp.append(condition.getLogicOperator());
        } else {
            tmp.append(" AND ");
        }
        tmp.append(" ");
        tmp.append(trimQuotes(condition.getLeftExpr()));
        tmp.append(" ");
        tmp.append(condition.getOperator());
        tmp.append(" ");
        if ("IN".equalsIgnoreCase(condition.getOperator())) {
            tmp.append("\n <foreach item=\"item\" collection=\"" + trimQuotes(condition.getRightExpr()) + "\" open=\"(\" separator=\",\" close=\")\">");
            tmp.append("\n #{item}");
            tmp.append("\n </foreach>");
        } else if ("LIKE".equalsIgnoreCase(condition.getOperator())) {
            tmp.append(getLikeConcatSql(trimQuotes(condition.getRightExpr())));
        } else {
            tmp.append(trimQuotes(condition.getRightExpr()));
            if (!Strings.isNullOrEmpty(condition.getMiddleOperator())) {
                tmp.append(" ");
                tmp.append(condition.getMiddleOperator());
                tmp.append(" ");
                tmp.append(trimQuotes(condition.getEndExpr()));
            }
        }
        tmp.append("\n</if>");
        return tmp.toString();
    }

    private String trimQuotes(String str) {
        if (Strings.isNullOrEmpty(str)
                || !str.startsWith("'")) {
            return str;
        }
        return str.substring(1, str.length() - 1);
    }

    private String getLikeConcatSql(String colExpr) {
        if (DbType.MYSQL == dataSourceConfig.getDbType()) {
            return "concat('%'," + colExpr + ",'%')";
        } else if (DbType.POSTGRE_SQL == dataSourceConfig.getDbType()) {
            return "concat('%'," + colExpr + ",'%')";
        } else if (DbType.ORACLE == dataSourceConfig.getDbType()) {
            return "'%' || " + colExpr + " ||'%'";
        } else if (DbType.SQL_SERVER == dataSourceConfig.getDbType()) {
            return "concat('%'," + colExpr + ",'%')";
        }
        return "concat('%'," + colExpr + ",'%')";
    }

}
