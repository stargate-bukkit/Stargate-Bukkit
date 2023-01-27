package org.sgrewritten.stargate.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sgrewritten.stargate.container.TwoTuple;

class SQLQueryHandlerTest {

    @ParameterizedTest
    @MethodSource("getAllPossibleQueryDatabaseCombinations")
    void getQuery(TwoTuple<SQLQuery,DatabaseDriver> tuple) {
        
        Assertions.assertTrue(SQLQueryHandler.getQuery(tuple.getFirstValue(), tuple.getSecondValue()).endsWith(";")," Invalid query, should end with ';'");
    }

    
    private static Stream<TwoTuple<SQLQuery,DatabaseDriver>> getAllPossibleQueryDatabaseCombinations() {
        List<TwoTuple<SQLQuery,DatabaseDriver>> allPossibleCombinations = new ArrayList<>();
        for(DatabaseDriver driver : DatabaseDriver.values()){
            for(SQLQuery query : SQLQuery.values()) {
                allPossibleCombinations.add(new TwoTuple<>(query,driver));
            }
        }
        
        return allPossibleCombinations.stream();
    }
}
