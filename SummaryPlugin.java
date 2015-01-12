/**
 * Copyright (c) 2003-2007, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *     following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of "David A. Czarnecki" and "blojsom" nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom", nor may "blojsom" appear in their name,
 *     without prior written permission of David A. Czarnecki.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.blojsom.plugin.summary;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.text.html.*;

/**
 * SummaryPlugin- is almost based on RSS Enclosure and File Attachment. 
 * Authors are thankful to Persistent Systems Pvt Ltd (http://www.persistentsys.com) for allowing them to develop 
 * this plugin.   
 *  @author Shalini Israni, Sopan Shewale
 *  @version 1.0
 * @since Blojsom Version 3.2
 */
public class SummaryPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(SummaryPlugin.class);
    private static final String SUMMARY_TEMPLATE = "org/blojsom/plugin/summary/admin-summary.vm";
    private static final String SUMMARY = "SUMMARY";
  
    private static final String METADATA_SUMMARY = "summary";

    protected EventBroadcaster _eventBroadcaster;
    protected ServletConfig _servletConfig;
    protected Properties _blojsomProperties;
    protected String _resourcesDirectory;
    
    private int minWords;
    private int maxWords;
    
    


    /**
     * Default constructor
     */
    public SummaryPlugin() {
    }
    
    

    /**
	 * @return Returns the maxWords.
	 */
	public int getMaxWords() {
		return maxWords;
	}



	/**
	 * @param maxWords The maxWords to set.
	 */
	public void setMaxWords(int maxWords) {
		this.maxWords = maxWords;

	}



	/**
	 * @return Returns the minWords.
	 */
	public int getMinWords() {
		return minWords;
	}



	/**
	 * @param minWords The minWords to set.
	 */
	public void setMinWords(int minWords) {
		this.minWords = minWords;

	}



	/**
     * Set the default blojsom properties
     *
     * @param blojsomProperties Default blojsom properties
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
    }

    /**
     * Set the {@link ServletConfig}
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }


    /**
     * Initialize this plugin. This method only called when the plugin is
     * instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _resourcesDirectory = _blojsomProperties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP, BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY);
        _eventBroadcaster.addListener(this);
    }


    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        ServletContext servletContext = _servletConfig.getServletContext();
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            if (BlojsomUtils.checkMapForKey(entry.getMetaData(), METADATA_SUMMARY)) {
                String summarydata = (String) entry.getMetaData().get(METADATA_SUMMARY);
                entry.getMetaData().put(METADATA_SUMMARY, summarydata);
                }
            else{
        		StringReader entryInHtmlForm = new StringReader(entry.getDescription());
        		HTMLEditorKit.Parser parse = new Preprocessor().getParser();
        		CallBackClass cbc = new CallBackClass(entry.getDescription());
        		
        		try {
        			parse.parse(entryInHtmlForm,cbc,true);
        		} catch (IOException e) {

        			e.printStackTrace();
        		}
            	
            	String summarydata = new Summary().generateSummary(cbc.getContent(),minWords,maxWords);
                entry.getMetaData().put(METADATA_SUMMARY, summarydata);


            }
            }
            return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ProcessEntryEvent) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Handling process blog entry event");
            }
            ProcessEntryEvent processBlogEntryEvent = (ProcessEntryEvent) event;
            String blogId = processBlogEntryEvent.getBlog().getBlogId();

            Map templateAdditions = (Map) processBlogEntryEvent.getContext().get("BLOJSOM_TEMPLATE_ADDITIONS");
            if (templateAdditions == null) {
                templateAdditions = new TreeMap();
            }

            templateAdditions.put(getClass().getName(), "#parse('" + SUMMARY_TEMPLATE + "')");
            processBlogEntryEvent.getContext().put("BLOJSOM_TEMPLATE_ADDITIONS", templateAdditions);

                 // Preserve the current summary 
            if (processBlogEntryEvent.getEntry() != null) {
                String currentSummary = (String) processBlogEntryEvent.getEntry().getMetaData().get(METADATA_SUMMARY);
                

                processBlogEntryEvent.getContext().put(SUMMARY, currentSummary);
               

            }
            String summary = BlojsomUtils.getRequestValue(METADATA_SUMMARY, processBlogEntryEvent.getHttpServletRequest());
            
                if (!BlojsomUtils.checkNullOrBlank(summary) && processBlogEntryEvent.getEntry() != null) {
                    processBlogEntryEvent.getEntry().getMetaData().put(METADATA_SUMMARY, summary);
                   processBlogEntryEvent.getContext().put(SUMMARY, summary);


                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Added/updated Summary: " + summary );
                    }
                } else {
                    if (processBlogEntryEvent.getEntry() != null) {
                        processBlogEntryEvent.getEntry().getMetaData().remove(METADATA_SUMMARY);
                    }
                    processBlogEntryEvent.getContext().remove(SUMMARY);
                }
            }

        }
    
    
    /**
     *@author Shalini Israni
     *
     * 
     */
    public class Summary {

    	
        private String summary;
        
        public Summary()
        {}

        /**
         *  
         * @param summary 
         */
        public Summary(String summary) {
           
            this.summary = summary;

        }



        /**
         * Get the summary
         *
         * @return summary string
         */
        public String getSummary() {
            return summary;

        }
        /**
         * 
         * @param entryDescription
         * @return
         */
        	public String generateSummary(String entryDescription,int minWords,int maxWords)
        	{
        		String markers = ".?!";
        		
        		//This will split the string on  a ? or . or a !  
        		StringTokenizer subString = new StringTokenizer(entryDescription,markers,true);		
        		
        		
        		
        		String summary = "";
        		while(subString.hasMoreTokens())
        		{
        			summary  += subString.nextToken() ;
        			StringTokenizer splittedSummary = new StringTokenizer(summary," ",true);
        			
        			if(splittedSummary.countTokens()>=minWords && splittedSummary.countTokens() <= maxWords)
        				return summary; 
        			
        			if(splittedSummary.countTokens() > maxWords)
        			{
        				int desiredNumWords = maxWords - (splittedSummary.countTokens() - maxWords);
        				
        				String truncatedSummary = "";
        				while(desiredNumWords>0){
        					truncatedSummary += splittedSummary.nextToken();
        					desiredNumWords-- ;
        				}
        				return truncatedSummary;
        			}
        			
        		}
        		return summary;
        	}

        

    }

 }

/**
 * 
 * @author Shalini Israni
 *This class is used to process the entry content and extract the text from within the HTML Tags for the summary generation purpose.
 */
class Preprocessor extends HTMLEditorKit
{

	  /**
	   * Call to obtain a HTMLEditorKit.Parser object.
	   * 
	   * @return A new HTMLEditorKit.Parser object.
	   */
	  public HTMLEditorKit.Parser getParser()
	  {
	    return super.getParser();
	  }
	}

/**
 * 
 * @author Shalini Israni
 *This class is needed for processing the HTML data
 */
class CallBackClass extends HTMLEditorKit.ParserCallback
{
	String htmlString ;
	private String content = "" ;
	public CallBackClass(String htmlString)
	{
		this.htmlString = htmlString;
	}
	public void handleText(char [] data,int pos)
	{
		String temp = new String(data);
		this.content += temp;
		
	}	
	
	public String getContent ()
	{
		return this.content;
	}
}


