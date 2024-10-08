# Select Statements

Select statements are the most complex SQL statements.  This library duplicates the syntax of the most common
select statements, but purposely does not cover every possibility.

In general, the following are supported:

1. The typical parts of a select statement including SELECT, DISTINCT, FROM, JOIN, WHERE, GROUP BY, UNION,
   UNION ALL, ORDER BY, HAVING
2. Tables can be aliased per select statement
3. Columns can be aliased per select statement
4. Some support for aggregates (avg, min, max, sum)
5. Joins of type INNER, LEFT OUTER, RIGHT OUTER, FULL OUTER
6. Subqueries in where clauses. For example, `where foo in (select foo from foos where id < 36)`
7. Select from another select. For example `select count(*) from (select foo from foos where id < 36)`
8. Multi-Selects. For example `(select * from foo order by id limit 3) union (select * from foo order by id desc limit 3)`

At this time, the library does not support the following:

1. WITH expressions
2. INTERSECT, EXCEPT, etc.

The user guide page for WHERE Clauses shows examples of many types of SELECT statements with different complexities of
the WHERE clause including support for sub-queries.  We will just show a single example here, including an ORDER BY
clause:

```java
SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
        .from(animalData)
        .where(id, isIn(1, 5, 7))
        .and(bodyWeight, isBetween(1.0).and(3.0))
        .orderBy(id.descending(), bodyWeight)
        .build()
        .render(RenderingStrategies.MYBATIS3);

List<AnimalData> animals = mapper.selectMany(selectStatement);
```

The WHERE and ORDER BY clauses are optional.

## Joins
The library supports the generation of join statements.  For example:

```java
SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
        .from(orderMaster, "om")
        .join(orderDetail, "od").on(orderMaster.orderId, isEqualTo(orderDetail.orderId))
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

Notice that you can give an alias to a table if desired. If you don't specify an alias, the full table name will be
used in the generated SQL.

Multiple tables can be joined in a single statement. For example:

```java
SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
        .from(orderMaster, "om")
        .join(orderLine, "ol").on(orderMaster.orderId, isEqualTo(orderLine.orderId))
        .join(itemMaster, "im").on(orderLine.itemId, isEqualTo(itemMaster.itemId))
        .where(orderMaster.orderId, isEqualTo(2))
        .build()
        .render(RenderingStrategies.MYBATIS3);
```
Join queries will likely require you to define a MyBatis result mapping in XML. This is the only instance where XML is
required.  This is due to the limitations of the MyBatis annotations when mapping collections.

The library supports four join types:

1. `.join(...)` is an INNER join
2. `.leftJoin(...)` is a LEFT OUTER join
3. `.rightJoin(...)` is a RIGHT OUTER join
4. `.fullJoin(...)` is a FULL OUTER join

## Union Queries
The library supports the generation of UNION and UNION ALL queries. For example:

```java
SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
        .from(animalData)
        .union()
        .selectDistinct(id, animalName, bodyWeight, brainWeight)
        .from(animalData)
        .orderBy(id)
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

Any number of SELECT statements can be added to a UNION query. Only one ORDER BY phrase is allowed.

With this type of union query, the "order by" and paging clauses are applied to the query as a whole. If
you need to apply "order by" or paging clauses to the nested queries, use a multi-select query as shown
below.

## Multi-Select Queries

Multi-select queries are a special case of union select statements. The difference is that "order by" and
paging clauses can be applied to the merged queries. For example:

```java
SelectStatementProvider selectStatement = multiSelect(
        select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .orderBy(id)
                .limit(2)
        ).union(
                selectDistinct(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .orderBy(id.descending())
                .limit(3)
        )
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

## MyBatis Mapper for Select Statements

The SelectStatementProvider object can be used as a parameter to a MyBatis mapper method directly. If you
are using an annotated mapper, the select method should look like this (note that we recommend coding a "selectMany"
and a "selectOne" method with a shared result mapping):

```java
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="AnimalDataResult", value={
        @Result(column="id", property="id", id=true),
        @Result(column="animal_name", property="animalName"),
        @Result(column="brain_weight", property="brainWeight"),
        @Result(column="body_weight", property="bodyWeight")
    })
    List<AnimalData> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("AnimalDataResult")
    AnimalData selectOne(SelectStatementProvider selectStatement);
...

```

## XML Mapper for Join Statements

If you are coding a join, it is likely you will need to code an XML mapper to define the result map. This is due to a
MyBatis limitation - the annotations cannot define a collection mapping. If you have to do this, the Java code looks
like this:

```java
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("SimpleJoinResult")
    List<OrderMaster> selectMany(SelectStatementProvider selectStatement);
```

And the corresponding XML looks like this:

```xml
<mapper namespace="examples.joins.JoinMapper">
  <resultMap id="SimpleJoinResult" type="examples.joins.OrderMaster">
    <id column="order_id" jdbcType="INTEGER" property="id" />
    <result column="order_date" jdbcType="DATE" property="orderDate" />
    <collection property="details" ofType="examples.joins.OrderDetail">
      <id column="order_id" jdbcType="INTEGER" property="orderId"/>
      <id column="line_number" jdbcType="INTEGER" property="lineNumber"/>
      <result column="description" jdbcType="VARCHAR" property="description"/>
      <result column="quantity" jdbcType="INTEGER" property="quantity"/>
    </collection>
  </resultMap>
</mapper>
```

Notice that the resultMap is the only element in the XML mapper. This is our recommended practice.

## XML Mapper for Select Statements
We do not recommend using an XML mapper for select statements, but if you want to do so the SelectStatementProvider
object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the select method should look like this in the Java interface:

```java
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

...
    List<AnimalData> selectMany(SelectStatementProvider selectStatement);
...

```

The XML element should look like this:

```xml
  <resultMap id="animalResult" type="examples.animal.data.AnimalData">
    <id column="id" jdbcType="INTEGER" property="id" />
    <result column="animal_name" jdbcType="VARCHAR" property="animalName" />
    <result column="brain_weight" jdbcType="DOUBLE" property="brainWeight" />
    <result column="body_weight" jdbcType="DOUBLE" property="bodyWeight" />
  </resultMap>

  <select id="selectMany" resultMap="animalResult">
    ${selectStatement}
  </select>
```

## Notes on Order By

Order by phrases can be difficult to calculate when there are aliased columns, aliased tables, unions, and joins.
This library has taken a relatively simple approach:

1. When specifying an SqlColumn in an ORDER BY phrase the library will either write the column alias or the column
   name into the ORDER BY phrase.  For the ORDER BY phrase, the table alias (if there is one) will be ignored. Use this
   pattern when the ORDER BY column is a member of the select list. For example `orderBy(foo)`. If the column has an
   alias, then it is easiest to use the "arbitrary string" method with the column alias as shown below.
2. It is also possible to explicitly specify a table alias for a column in an ORDER BY phrase. Use this pattern when
   there is a join, and the ORDER BY column is in two or more tables, and the ORDER BY column is not in the select
   list. For example `orderBy(sortColumn("t1", foo))`.
3. If none of the above use cases meet your needs, then you can specify an arbitrary String to write into the rendered
   ORDER BY phrase (see below for an example).

In our testing, this caused an issue in only one case.  When there is an outer join and the select list contains
both the left and right join column.  In that case, the workaround is to supply a column alias for both columns.

When using a column function (lower, upper, etc.), then it is customary to give the calculated column an alias so you
will have a predictable result set.  In cases like this there will not be a column to use for an alias.  The library
supports arbitrary values in an ORDER BY expression like this:

```java
SelectStatementProvider selectStatement = select(substring(gender, 1, 1).as("ShortGender"), avg(age).as("AverageAge"))
        .from(person, "a")
        .groupBy(substring(gender, 1, 1))
        .orderBy(sortColumn("ShortGender").descending())
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

In this example the `substring` function is used in both the select list and the GROUP BY expression.  In the ORDER BY
expression, we use the `sortColumn` function to duplicate the alias given to the column in the select list.

## Limit and Offset Support
Since version 1.1.1 the select statement supports limit and offset for paging (or slicing) queries. You can specify:

- Limit only
- Offset only
- Both limit and offset

It is important to note that the select renderer writes limit and offset clauses into the generated select statement as
is. The library does not attempt to normalize those values for databases that don't support limit and offset directly.
Therefore, it is very important for users to understand whether the target database supports limit and offset.
If the target database does not support limit and offset, then it is likely that using this support will create SQL
that has runtime errors.

An example follows:

```java
SelectStatementProvider selectStatement = select(animalData.allColumns())
        .from(animalData)
        .orderBy(id)
        .limit(3)
        .offset(22)
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

## Fetch First Support
Since version 1.1.2 the select statement supports fetch first for paging (or slicing) queries. You can specify:

- Fetch first only
- Offset only
- Both offset and fetch first

Fetch first is an SQL standard and is supported by most databases.

An example follows:

```java
SelectStatementProvider selectStatement = select(animalData.allColumns())
        .from(animalData)
        .orderBy(id)
        .offset(22)
        .fetchFirst(3).rowsOnly()
        .build()
        .render(RenderingStrategies.MYBATIS3);
```
