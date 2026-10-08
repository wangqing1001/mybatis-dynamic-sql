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
package org.mybatis.dynamic.sql.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class FragmentCollectorTest {

    @Test
    void testWhereFragmentCollectorMerge() {
        FragmentAndParameters fp1 = new FragmentAndParameters(":p1", Collections.singletonMap("p1", 1));
        FragmentAndParameters fp2 = new FragmentAndParameters(":p2", Collections.singletonMap("p2", 2));
        FragmentCollector fc1 =  CollectorExtKt.toFragmentCollector(List.of(fp1,fp2));
        assertThat(fc1.collectFragments(",")).isEqualTo(":p1,:p2");
        assertThat(fc1.parameters()).containsOnly(entry("p1", 1), entry("p2", 2));
    }
}
