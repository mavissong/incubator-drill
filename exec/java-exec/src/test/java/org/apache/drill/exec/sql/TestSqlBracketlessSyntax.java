/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.sql;

import net.hydromatic.optiq.config.Lex;
import net.hydromatic.optiq.tools.Frameworks;
import net.hydromatic.optiq.tools.Planner;
import net.hydromatic.optiq.tools.StdFrameworkConfig;

import org.apache.drill.exec.planner.sql.DrillConvertletTable;
import org.apache.drill.exec.planner.sql.parser.CompoundIdentifierConverter;
import org.apache.drill.exec.planner.sql.parser.impl.DrillParserImpl;
import org.apache.drill.test.DrillAssert;
import org.eigenbase.sql.SqlNode;
import org.junit.Test;

public class TestSqlBracketlessSyntax {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestSqlBracketlessSyntax.class);

  @Test
  public void checkComplexExpressionParsing() throws Exception{
    StdFrameworkConfig config = StdFrameworkConfig.newBuilder() //
        .lex(Lex.MYSQL) //
        .parserFactory(DrillParserImpl.FACTORY) //
        .defaultSchema(Frameworks.createRootSchema(false)) //
        .convertletTable(new DrillConvertletTable()) //
        .build();
    Planner planner = Frameworks.getPlanner(config);

    SqlNode node = planner.parse(""
        + "select a[4].c \n"
        + "from x.y.z \n"
        + "where a.c.b = 5 and x[2] = 7 \n"
        + "group by d \n"
        + "having a.c < 5 \n"
        + "order by x.a.a.a.a.a");

    String expected = "SELECT `a`[4]['c']\n" +
        "FROM `x`.`y`.`z`\n" +
        "WHERE `a`.`c`['b'] = 5 AND `x`[2] = 7\n" +
        "GROUP BY `d`\n" +
        "HAVING `a`.`c` < 5\n" +
        "ORDER BY `x`.`a`['a']['a']['a']['a']";


    SqlNode rewritten = node.accept(new CompoundIdentifierConverter());
    String rewrittenQuery = rewritten.toString();

    DrillAssert.assertMultiLineStringEquals(expected, rewrittenQuery);
  }


}
