/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * #SNAPSHOT#
 */
package fr.jayasoft.ivy.xml;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.DefaultArtifact;
import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.ModuleId;
import fr.jayasoft.ivy.ModuleRevisionId;
import fr.jayasoft.ivy.report.XmlReportOutputter;

public class XmlReportParser {
    private static class SaxXmlReportParser {
        private List _mrids;
        private List _defaultMrids;
        private List _realMrids;
		private List _artifacts;
		private File _report;
		SaxXmlReportParser(File report) {
	        _artifacts = new ArrayList();
            _mrids = new ArrayList();
            _defaultMrids = new ArrayList();
            _realMrids = new ArrayList();
			_report = report;
		}
		
		public void parse() throws Exception {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(_report, new DefaultHandler() {
                private String _organisation;
                private String _module;
                private String _revision;
                private Date _pubdate;
                private boolean _skip;
				private ModuleRevisionId _mrid;
                private boolean _default;
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("module".equals(qName)) {
                        _organisation = attributes.getValue("organisation");
                        _module = attributes.getValue("name");
                    } else if ("revision".equals(qName)) {
                        _revision = attributes.getValue("name");
                        _default = Boolean.valueOf(attributes.getValue("default")).booleanValue();
                        if (attributes.getValue("error") != null || attributes.getValue("evicted") != null) {
                            _skip = true;
                        } else {
	                        _mrid = ModuleRevisionId.newInstance(_organisation, _module, _revision);
							_mrids.add(_mrid);
                            if (_default) {
                                _defaultMrids.add(_mrid);
                            } else {
                                _realMrids.add(_mrid);
                            }
                            try {
                                _pubdate = Ivy.DATE_FORMAT.parse(attributes.getValue("pubdate"));
                                _skip = false;
                            } catch (ParseException e) {
                                throw new IllegalArgumentException("invalid publication date for "+_organisation+" "+_module+" "+_revision+": "+attributes.getValue("pubdate"));
                            }
                        }
                    } else if ("artifact".equals(qName)) {
                        if (_skip) {
                            return;
                        }
                        String status = attributes.getValue("status");
                        if (status != null && "failed".equals(status)) {
                            return;
                        }
                        String artifactName = attributes.getValue("name");
                        String type = attributes.getValue("type");
                        String ext = attributes.getValue("ext");
						Artifact artifact = new DefaultArtifact(_mrid, _pubdate, artifactName, type, ext);
                        _artifacts.add(artifact);
                    }
                }
            });			
		}

		public List getArtifacts() {
			return _artifacts;
		}
		public List getModuleRevisionIds() {
			return _mrids;
		}

        public List getRealModuleRevisionIds() {
            return _realMrids;
        }
    }

	public Artifact[] getArtifacts(ModuleId moduleId, String conf, File cache) throws ParseException, IOException {
        File report = new File(cache, XmlReportOutputter.getReportFileName(moduleId, conf));
        if (!report.exists()) {
            throw new IllegalStateException("no report file found for "+moduleId+" "+conf+" in "+cache+": ivy was looking for "+report);
        }
		return getArtifacts(report);
    }

	private Artifact[] getArtifacts(File report) throws ParseException {
		try {
			SaxXmlReportParser parser = new SaxXmlReportParser(report);
			parser.parse();
            return (Artifact[])parser.getArtifacts().toArray(new Artifact[parser.getArtifacts().size()]);
        } catch (Exception ex) {
            ParseException pe = new ParseException("failed to parse report: "+report+": "+ex.getMessage(), 0);
            pe.initCause(ex);
            throw pe;
        }
	}
        
    public ModuleRevisionId[] getDependencyRevisionIds(ModuleId moduleId, String conf, File cache) throws ParseException, IOException {
        File report = new File(cache, XmlReportOutputter.getReportFileName(moduleId, conf));
        if (!report.exists()) {
            throw new IllegalStateException("no report file found for "+moduleId+" "+conf+" in "+cache+": ivy was looking for "+report);
        }
        return getDependencyRevisionIds(report);
    }

    private ModuleRevisionId[] getDependencyRevisionIds(File report) throws ParseException {
        try {
            SaxXmlReportParser parser = new SaxXmlReportParser(report);
            parser.parse();
            return (ModuleRevisionId[])parser.getModuleRevisionIds().toArray(new ModuleRevisionId[parser.getModuleRevisionIds().size()]);
        } catch (Exception ex) {
            ParseException pe = new ParseException("failed to parse report: "+report+": "+ex.getMessage(), 0);
            pe.initCause(ex);
            throw pe;
        }
    }
        
    /**
     * Returns all the mrids of the dependencies which have a real module descriptor, i.e. not a default one
     */
    public ModuleRevisionId[] getRealDependencyRevisionIds(ModuleId moduleId, String conf, File cache) throws ParseException, IOException {
        File report = new File(cache, XmlReportOutputter.getReportFileName(moduleId, conf));
        if (!report.exists()) {
            throw new IllegalStateException("no report file found for "+moduleId+" "+conf+" in "+cache+": ivy was looking for "+report);
        }
        return getRealDependencyRevisionIds(report);
    }

    private ModuleRevisionId[] getRealDependencyRevisionIds(File report) throws ParseException {
        try {
            SaxXmlReportParser parser = new SaxXmlReportParser(report);
            parser.parse();
            return (ModuleRevisionId[])parser.getRealModuleRevisionIds().toArray(new ModuleRevisionId[parser.getRealModuleRevisionIds().size()]);
        } catch (Exception ex) {
            ParseException pe = new ParseException("failed to parse report: "+report+": "+ex.getMessage(), 0);
            pe.initCause(ex);
            throw pe;
        }
    }
        
    
}
