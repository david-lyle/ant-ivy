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
package org.apache.ivy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.Credentials;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.CredentialsStore;
import org.junit.Test;

public class DefaultConfigurationTest {

    @Test
    public void willStoreCredentialsTest() throws ParseException, IOException {

        String host = "lyleco-760749160151.d.codeartifact.us-west-2.amazonaws.com";

        IvySettings settings = new IvySettings();
        Ivy ivy = Ivy.newInstance(settings);
        ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));

        ModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            ModuleRevisionId.newInstance("com.databricks", "dbc-parent", "1.0"));

        ResolveOptions resolveOptions = new ResolveOptions();

        ivy.resolve(md, resolveOptions);

        CredentialsStore creds = CredentialsStore.INSTANCE;
        System.out.println(settings);
        assertTrue("Requried Creds have not be stored.", creds.hasCredentials(host));
        Credentials c = creds.getCredentials("lyleco/dlyle-test", host);
        assertEquals("Username wasn't correct", "aws", c.getUserName());
        assertEquals("Password wasn't correct", "password", c.getPasswd());
    }

}
