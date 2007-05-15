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
package org.apache.ivy.ant;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.ivy.core.cache.CacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.plugins.report.XmlReportParser;
import org.apache.ivy.util.Message;
import org.apache.tools.ant.BuildException;


/**
 * Base class for the cache path related classes: cachepath and cachefileset.
 * 
 * Most of the behviour is common to the two, since only the produced element differs.
 * 
 */
public abstract class IvyCacheTask extends IvyPostResolveTask {

    protected List getArtifacts() throws BuildException, ParseException, IOException {
        Collection artifacts = getAllArtifacts();
        List ret = new ArrayList();
        for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
            Artifact artifact = (Artifact)iter.next();
            if (getArtifactFilter().accept(artifact)) {
            	ret.add(artifact);
            }
        }
        
        return ret;
    }

    private Collection getAllArtifacts() throws ParseException, IOException {
        String[] confs = splitConfs(getConf());
        Collection all = new LinkedHashSet();

        ResolveReport report = getResolvedReport();
        if (report != null) {
            Message.debug("using internal report instance to get artifacts list");
            for (int i = 0; i < confs.length; i++) {
                ConfigurationResolveReport configurationReport = report.getConfigurationReport(confs[i]);
                if (configurationReport == null) {
                	throw new BuildException("bad confs provided: "+confs[i]+" not found among "+Arrays.asList(report.getConfigurations()));
                }
				Set revisions = configurationReport.getModuleRevisionIds();
                for (Iterator it = revisions.iterator(); it.hasNext(); ) {
                	ModuleRevisionId revId = (ModuleRevisionId) it.next();
                	ArtifactDownloadReport[] aReps = configurationReport.getDownloadReports(revId);
                	for (int j = 0; j < aReps.length; j++) {
                		all.add(aReps[j].getArtifact());
                	}
                }
            }
        } else {
            Message.debug("using stored report to get artifacts list");
            
            XmlReportParser parser = new XmlReportParser();
            CacheManager cacheMgr = getIvyInstance().getCacheManager(getCache());
            for (int i = 0; i < confs.length; i++) {
            	File reportFile = cacheMgr.getConfigurationResolveReportInCache(getResolveId(), confs[i]);
            	parser.parse(reportFile);
            	
                Artifact[] artifacts = parser.getArtifacts();
                all.addAll(Arrays.asList(artifacts));
            }
        }
        return all;
    }

	protected CacheManager getCacheManager() {
		CacheManager cache = new CacheManager(getSettings(), getCache());
		return cache;
	}
}
