/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.plugins.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Properties;

import org.apache.ivy.plugins.tools.SparkPropertiesParser.SparkPropertiesParserBuilder;
import org.junit.Test;

public class SparkPropertiesParserBuilderTest {

    @Test(expected = InvalidParameterException.class)
    public void builderRequiresAtLeastOneParameter() {
        try {
            SparkPropertiesParserBuilder.newInstance().build();
        } catch (IOException e) {
        }
    }

    @Test
    public void builderIsolatesCredentialsProperties()
            throws InvalidParameterException, IOException {

        Properties properties = new Properties();
        File testFile = new File("test/tools/mixed.properties");
        properties.load(new FileInputStream(testFile));

        SparkPropertiesParserBuilder builder = SparkPropertiesParserBuilder.newInstance();
        builder.fromProperties(properties);
        Map<Integer, Map<String, String>> testCredMap = builder.build()
                .getCredentialsPropertiesMap();

        assertEquals("Output Credentials Map Not Correct Size", 2, testCredMap.size());
        assertEquals("Credentials 0 Map Not Correct Size", 5, testCredMap.get(0).size());

    }

    @Test
    public void findTestFile() {
        String path = "src/test/tools";

        File file = new File(path);
        String absolutePath = file.getAbsolutePath();

        System.out.println(absolutePath);

    }
}
