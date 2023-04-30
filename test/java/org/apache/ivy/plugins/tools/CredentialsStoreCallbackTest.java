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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class CredentialsStoreCallbackTest {

    @Test
    public void testPropertyMapping() throws FileNotFoundException, IOException {

        Properties properties = new Properties();
        File testFile = new File("test/tools/credential.properties");
        properties.load(new FileInputStream(testFile));
        Map<String, String> credential = new HashMap<>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {

            CredentalsStoreCallback.addCredentialKV(entry.getKey().toString(),
                entry.getValue().toString(), credential);

        }

        assertEquals("addCredentialKV Lost a Property", 6, credential.size());
        assertTrue("Credential does not contain realm.", credential.containsKey("realm"));
        assertTrue("Credential does not contain host.", credential.containsKey("host"));
        assertTrue("Credential does not contain userName.", credential.containsKey("userName"));
        assertTrue("Credential does not contain password.", credential.containsKey("passwd"));

    }

}
