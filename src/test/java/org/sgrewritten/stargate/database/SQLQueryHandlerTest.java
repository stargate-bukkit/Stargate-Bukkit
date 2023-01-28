package org.sgrewritten.stargate.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sgrewritten.stargate.container.TwoTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class SQLQueryHandlerTest {

    @ParameterizedTest
    @MethodSource("getAllPossibleQueryDatabaseCombinations")
    void getQuery(TwoTuple<SQLQuery, DatabaseDriver> tuple) {
        String query = SQLQueryHandler.getQuery(tuple.getFirstValue(), tuple.getSecondValue());
        Assertions.assertTrue(query.strip().endsWith(";"), " Invalid query, should end with ';'");
    }

    private static Stream<TwoTuple<SQLQuery, DatabaseDriver>> getAllPossibleQueryDatabaseCombinations() {
        List<TwoTuple<SQLQuery, DatabaseDriver>> allPossibleCombinations = new ArrayList<>();
        for (DatabaseDriver driver : DatabaseDriver.values()) {
            for (SQLQuery query : SQLQuery.values()) {
                allPossibleCombinations.add(new TwoTuple<>(query, driver));
            }
        }

        return allPossibleCombinations.stream();
    }

}
