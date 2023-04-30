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
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.ivy.Ivy;
import org.apache.ivy.Ivy.IvyCallback;
import org.apache.ivy.core.IvyContext;
import org.apache.ivy.plugins.tools.SparkPropertiesParser.SparkPropertiesParserBuilder;
import org.apache.ivy.util.url.CredentialsStore;

public class CredentalsStoreCallback implements IvyCallback {

    private Map<Integer, Map<String, String>> mavenCredentials;

    @SuppressWarnings("unused")
    private CredentalsStoreCallback() {

    }

    public CredentalsStoreCallback(String filePath) throws InvalidParameterException, IOException {

        mavenCredentials = SparkPropertiesParserBuilder.newInstance().fromFile(filePath).build()
                .getCredentialsPropertiesMap();

    }

    public CredentalsStoreCallback(Properties properties)
            throws InvalidParameterException, IOException {

        mavenCredentials = SparkPropertiesParserBuilder.newInstance().fromProperties(properties)
                .build().getCredentialsPropertiesMap();
    }

    @Override
    public Object doInIvyContext(Ivy ivy, IvyContext context) {

        addCredentialsToStore();

        return null;

    }

    static void addCredentialKV(String key, String value, Map<String, String> credential) {

        if (key.contains("realm")) {

            credential.put("realm", value);

        } else if (key.contains("host")) {

            credential.put("host", value);

        } else if (key.contains("user")) {

            credential.put("userName", value);

        } else if (key.contains("passwd")) {

            credential.put("passwd", value);

        } else if (key.contains("aws.domain")) {

            credential.put("aws.domain", value);

        } else if (key.contains("aws.acct")) {

            credential.put("aws.acct", value);

        }

    }

    private void addCredentialsToStore() {

        if (null == mavenCredentials || mavenCredentials.size() == 0) {
            return;
        }

        Map<String, String> credential = new HashMap<>();

        for (int i = 0; i < mavenCredentials.size(); i++) {

            Set<Entry<String, String>> props = mavenCredentials.get(i).entrySet();

            credential.clear();

            for (Map.Entry<String, String> entry : props) {

                addCredentialKV(entry.getKey(), entry.getValue(), credential);

            }

            // credential is complete - check for aws token
            if (credential.get("userName").equals("aws") && !credential.containsKey("passwd")) {

                if (!credential.containsKey("aws.domain") || !credential.containsKey("aws.acct")) {
                    throw new InvalidParameterException(
                            "AWS Token Renewal Requires aws.domain and aws.acct Properties");
                }

                credential.put("passwd", AWSCodeartifactCredentialsProvider
                        .generateKey(credential.get("domain"), credential.get("domain.owner")));

            }

            CredentialsStore.INSTANCE.addCredentials(credential.get("realm"),
                credential.get("host"), credential.get("userName"), credential.get("passwd"));

        }

    }

}
