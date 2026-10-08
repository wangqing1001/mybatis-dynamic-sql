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
package org.mybatis.dynamic.sql.insert.render;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.util.FieldAndValueAndParameters;
import org.mybatis.dynamic.sql.util.FieldAndValueCollector;

import java.util.List;

class FieldAndValueCollectorTest {

    @Test
    void testMerge() {
        FieldAndValueAndParameters fvp1 = new FieldAndValueAndParameters("f1","3");
        FieldAndValueAndParameters fvp2 = new FieldAndValueAndParameters("f2","4");
        FieldAndValueCollector collector1 = new FieldAndValueCollector(List.of(fvp1,fvp2));
        assertThat(collector1.columnsPhrase()).isEqualTo("(f1, f2)");
        assertThat(collector1.valuesPhrase()).isEqualTo("values (3, 4)");
    }
}
