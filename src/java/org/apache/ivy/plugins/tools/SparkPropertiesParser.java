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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparkPropertiesParser {

    final private Properties properties;

    final private static String CREDENTIAL_MATCH = "spark.databricks.driver.maven.credential";

    private Map<Integer, Map<String, String>> credentialsProperties;

    @SuppressWarnings("unused")
    private SparkPropertiesParser() {
        this.properties = new Properties();
    }

    private SparkPropertiesParser(String file) throws IOException {

        Map<String, String> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get(file))) {
            lines.filter(SparkPropertiesParser::credentailMatch).forEach(line -> {
                String[] keyValuePair = line.split(" ", 2);
                String key = keyValuePair[0];
                String value = keyValuePair[1];
                map.putIfAbsent(key, value);
            });
        } catch (IOException e) {
            throw e;
        }

        properties = new Properties();
        properties.putAll(map);

        this.populateCredentialsProperties();

    }

    private static boolean credentailMatch(String s) {
        return s.contains(CREDENTIAL_MATCH);
    }

    private static boolean credentialMatch(Map.Entry<Object, Object> e) {
        return credentailMatch(e.getKey().toString());
    }

    private SparkPropertiesParser(Properties properties) {

        this.properties = fromEntrySet(properties.entrySet());
        this.populateCredentialsProperties();

    }

    private static Properties fromEntrySet(Set<Entry<Object, Object>> entrySet) {

        Properties retSet = new Properties();
        retSet.putAll(entrySet.stream().filter(SparkPropertiesParser::credentialMatch)
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                    e -> String.valueOf(e.getValue()), (prev, next) -> next, HashMap::new)));
        return retSet;

    }

    private void populateCredentialsProperties() {
        credentialsProperties = new HashMap<>();
        Properties workingSet = fromEntrySet(properties.entrySet());

        int i = 0;
        while (true) {
            // grab the ith indexed credential set and put in hashmap keyed by index
            int count = i;
            HashMap<String, String> creds = new HashMap<>();
            creds.putAll(workingSet.entrySet().stream()
                    .filter(e -> (e.getKey().toString().contains(String.valueOf(count))))
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()), (prev, next) -> next, HashMap::new)));

            if (creds.isEmpty()) {
                break;
            }

            credentialsProperties.put(i, creds);
            Set<Entry<Object, Object>> we = workingSet.entrySet();
            we.removeAll(creds.entrySet());
            workingSet = fromEntrySet(we);
            i++;
        }

    }

    public Map<Integer, Map<String, String>> getCredentialsPropertiesMap() {
        return credentialsProperties;
    }

    public static class SparkPropertiesParserBuilder {

        private String fileName;

        private Properties properties;

        public static SparkPropertiesParserBuilder newInstance() {
            return new SparkPropertiesParserBuilder();
        }

        public SparkPropertiesParserBuilder fromFile(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public SparkPropertiesParserBuilder fromProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public SparkPropertiesParser build() throws InvalidParameterException, IOException {

            if (null == fileName && null == properties) {
                throw new InvalidParameterException("Must Specify File Name or Properties");
            }

            if (null == fileName) {
                return new SparkPropertiesParser(properties);
            }

            return new SparkPropertiesParser(fileName);

        }

    }
}
