/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.sqlparser.druid;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOrderingExpr;
import io.seata.sqlparser.ParametersHolder;
import io.seata.sqlparser.SQLType;
import io.seata.sqlparser.druid.h2.H2DeleteRecognizer;
import io.seata.sqlparser.util.JdbcConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * The type h2 sql delete recognizer test.
 *
 * @author hongyan 2021-11-12
 */
public class H2DeleteRecognizerTest extends AbstractRecognizerTest {

    @Test
    public void testVMarker() {
        Assertions.assertEquals("?", new BaseRecognizer.VMarker().toString());
    }

    /**
     * Delete recognizer test 0.
     */
    @Test
    public void deleteRecognizerTest_0() {

        String sql = "DELETE FROM t1 WHERE id = 'id1'";

        SQLStatement statement = getSQLStatement(sql);

        H2DeleteRecognizer h2DeleteRecognizer = new H2DeleteRecognizer(sql, statement);
        Assertions.assertEquals(sql, h2DeleteRecognizer.getOriginalSQL());
        Assertions.assertEquals("t1", h2DeleteRecognizer.getTableName());
        Assertions.assertEquals("id = 'id1'", h2DeleteRecognizer.getWhereCondition());
        sql = "DELETE FROM t1 WHERE id > 1";
        statement = getSQLStatement(sql);
        h2DeleteRecognizer = new H2DeleteRecognizer(sql, statement);
        Assertions.assertEquals("", h2DeleteRecognizer.getLimitCondition());

    }

    /**
     * Delete recognizer test 1.
     */
    @Test
    public void deleteRecognizerTest_1() {

        String sql = "DELETE FROM t1 WHERE id = ?";

        SQLStatement statement = getSQLStatement(sql);

        H2DeleteRecognizer h2DeleteRecognizer = new H2DeleteRecognizer(sql, statement);

        Assertions.assertEquals(sql, h2DeleteRecognizer.getOriginalSQL());
        Assertions.assertEquals("t1", h2DeleteRecognizer.getTableName());

        // test overflow parameters
        ArrayList<List<Object>> paramAppenderList = new ArrayList<>();
        String whereCondition = h2DeleteRecognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                ArrayList<Object> idParam = new ArrayList<>();
                idParam.add("id1");
                Map result = new HashMap<>();
                result.put(1,idParam);
                return result;
            }
        }, paramAppenderList);

        Assertions.assertEquals(Collections.singletonList(Collections.singletonList("id1")), paramAppenderList);
        Assertions.assertEquals("id = ?", whereCondition);
    }

    /**
     * Delete recognizer test 2.
     */
    @Test
    public void deleteRecognizerTest_2() {

        String sql = "DELETE FROM t1 WHERE id IN (?, ?)";

        SQLStatement statement = getSQLStatement(sql);

        H2DeleteRecognizer h2DeleteRecognizer = new H2DeleteRecognizer(sql, statement);

        Assertions.assertEquals(sql, h2DeleteRecognizer.getOriginalSQL());
        Assertions.assertEquals("t1", h2DeleteRecognizer.getTableName());

        // test overflow parameters
        ArrayList<List<Object>> paramAppenderList = new ArrayList<>();
        String whereCondition = h2DeleteRecognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                ArrayList<Object> idParam = new ArrayList<>();
                idParam.add("id1");
                ArrayList<Object> id2Param = new ArrayList<>();
                id2Param.add("id2");
                Map result = new HashMap();
                result.put(1, idParam);
                result.put(2, id2Param);
                return result;
            }
        }, paramAppenderList);

        Assertions.assertEquals(Arrays.asList(Arrays.asList("id1", "id2")), paramAppenderList);
        Assertions.assertEquals("id IN (?, ?)", whereCondition);
    }

    /**
     * Delete recognizer test 3.
     */
    @Test
    public void deleteRecognizerTest_3() {

        String sql = "DELETE FROM t1 WHERE id between ? AND ?";

        SQLStatement statement = getSQLStatement(sql);

        H2DeleteRecognizer h2DeleteRecognizer = new H2DeleteRecognizer(sql, statement);

        Assertions.assertEquals(sql, h2DeleteRecognizer.getOriginalSQL());
        Assertions.assertEquals("t1", h2DeleteRecognizer.getTableName());

        // test overflow parameters
        ArrayList<List<Object>> paramAppenderList = new ArrayList<>();
        String whereCondition = h2DeleteRecognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                ArrayList<Object> idParam = new ArrayList<>();
                idParam.add("id1");
                ArrayList<Object> id2Param = new ArrayList<>();
                id2Param.add("id2");
                Map result = new HashMap();
                result.put(1, idParam);
                result.put(2, id2Param);
                return result;
            }
        }, paramAppenderList);

        Assertions.assertEquals(Collections.singletonList(Arrays.asList("id1", "id2")), paramAppenderList);
        Assertions.assertEquals("id BETWEEN ? AND ?", whereCondition);
    }

    @Test
    public void testGetSqlType() {
        String sql = "delete from t where id = ?";
        List<SQLStatement> asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);

        H2DeleteRecognizer recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        Assertions.assertEquals(recognizer.getSQLType(), SQLType.DELETE);
    }

    @Test
    public void testGetTableAlias() {
        String sql = "delete from t where id = ?";
        List<SQLStatement> asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);

        H2DeleteRecognizer recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        Assertions.assertNull(recognizer.getTableAlias());
    }

    @Test
    public void testGetWhereCondition_0() {
        String sql = "delete from t";
        List<SQLStatement> asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);

        H2DeleteRecognizer recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        String whereCondition = recognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                return null;
            }
        }, new ArrayList<>());

        //test for no condition
        Assertions.assertEquals("", whereCondition);

        sql = "delete from t where id = ?";
        asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);

        recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        whereCondition = recognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                ArrayList<Object> idParam = new ArrayList<>();
                idParam.add(1);
                Map result = new HashMap();
                result.put(1, idParam);
                return result;
            }
        }, new ArrayList<>());

        //test for normal sql
        Assertions.assertEquals("id = ?", whereCondition);

        sql = "delete from t where id in (?)";
        asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);
        recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        whereCondition = recognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                ArrayList<Object> idParam = new ArrayList<>();
                idParam.add(1);
                Map result = new HashMap();
                result.put(1, idParam);
                return result;
            }
        }, new ArrayList<>());

        //test for sql with in
        Assertions.assertEquals("id IN (?)", whereCondition);

        sql = "delete from t where id between ? and ?";
        asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);
        recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        whereCondition = recognizer.getWhereCondition(new ParametersHolder() {
            @Override
            public Map<Integer,ArrayList<Object>> getParameters() {
                ArrayList<Object> idParam = new ArrayList<>();
                idParam.add(1);
                ArrayList<Object> idParam2 = new ArrayList<>();
                idParam.add(2);
                Map result = new HashMap();
                result.put(1, idParam);
                result.put(2, idParam2);
                return result;
            }
        }, new ArrayList<>());
        //test for sql with in
        Assertions.assertEquals("id BETWEEN ? AND ?", whereCondition);

        //test for exception
//        Assertions.assertThrows(IllegalArgumentException.class, () -> {
//            String s = "delete from t where id in (?)";
//            List<SQLStatement> sqlStatements = SQLUtils.parseStatements(s, JdbcConstants.H2);
//            SQLDeleteStatement deleteAst = (SQLDeleteStatement) sqlStatements.get(0);
//            deleteAst.setWhere(null);
//            new H2DeleteRecognizer(s, deleteAst).getWhereCondition(new ParametersHolder() {
//                @Override
//                public Map<Integer,ArrayList<Object>> getParameters() {
//                    return new HashMap();
//                }
//            }, new ArrayList<>());
//        });
    }

    @Test
    public void testGetWhereCondition_1() {

        String sql = "delete from t";
        List<SQLStatement> asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);

        H2DeleteRecognizer recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        String whereCondition = recognizer.getWhereCondition();

        //test for no condition
        Assertions.assertEquals("", whereCondition);

        sql = "delete from t where id = 1";
        asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);

        recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        whereCondition = recognizer.getWhereCondition();

        //test for normal sql
        Assertions.assertEquals("id = 1", whereCondition);

        sql = "delete from t where id in (1)";
        asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);
        recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        whereCondition = recognizer.getWhereCondition();

        //test for sql with in
        Assertions.assertEquals("id IN (1)", whereCondition);

        sql = "delete from t where id between 1 and 2";
        asts = SQLUtils.parseStatements(sql, JdbcConstants.H2);
        recognizer = new H2DeleteRecognizer(sql, asts.get(0));
        whereCondition = recognizer.getWhereCondition();
        //test for sql with in
        Assertions.assertEquals("id BETWEEN 1 AND 2", whereCondition);

        //test for exception
//        Assertions.assertThrows(IllegalArgumentException.class, () -> {
//            String s = "delete from t where id in (1)";
//            List<SQLStatement> sqlStatements = SQLUtils.parseStatements(s, JdbcConstants.H2);
//            SQLDeleteStatement deleteAst = (SQLDeleteStatement) sqlStatements.get(0);
//            deleteAst.setWhere(null);
//            new H2DeleteRecognizer(s, deleteAst).getWhereCondition();
//        });
    }

    @Override
    public String getDbType() {
        return JdbcConstants.H2;
    }
}
