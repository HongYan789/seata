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
package io.seata.sqlparser.druid.h2;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import io.seata.common.loader.LoadLevel;
import io.seata.sqlparser.SQLRecognizer;
import io.seata.sqlparser.druid.SQLOperateRecognizerHolder;
import io.seata.sqlparser.util.JdbcConstants;
/**
 * @author hongyan
 * @date Created in 2021-11-09 17:06
 * @description
 */
@LoadLevel(name = JdbcConstants.H2)
public class H2OperateRecognizerHolder implements SQLOperateRecognizerHolder {

    public H2OperateRecognizerHolder() {
    }

    @Override
    public SQLRecognizer getDeleteRecognizer(String sql, SQLStatement ast) {
        return new H2DeleteRecognizer(sql, ast);
    }
    @Override
    public SQLRecognizer getInsertRecognizer(String sql, SQLStatement ast) {
        return new H2InsertRecognizer(sql, ast);
    }
    @Override
    public SQLRecognizer getUpdateRecognizer(String sql, SQLStatement ast) {
        return new H2UpdateRecognizer(sql, ast);
    }
    @Override
    public SQLRecognizer getSelectForUpdateRecognizer(String sql, SQLStatement ast) {
        return ((SQLSelectStatement)ast).getSelect().getFirstQueryBlock().isForUpdate() ? new H2SelectForUpdateRecognizer(sql, ast) : null;
    }
}

