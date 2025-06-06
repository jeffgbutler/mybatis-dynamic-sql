/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.simple;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.jspecify.annotations.Nullable;

public class LastNameTypeHandler implements TypeHandler<LastName> {

    @Override
    public void setParameter(PreparedStatement ps, int i, @Nullable LastName parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            ps.setString(i, parameter.name());
        }
    }

    @Override
    public LastName getResult(ResultSet rs, String columnName) throws SQLException {
        return toLastName(rs.getString(columnName));
    }

    @Override
    public LastName getResult(ResultSet rs, int columnIndex) throws SQLException {
        return toLastName(rs.getString(columnIndex));
    }

    @Override
    public LastName getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toLastName(cs.getString(columnIndex));
    }

    private @Nullable LastName toLastName(@Nullable String s) {
        return s == null ? null : new LastName(s);
    }
}
