package org.concordion.ext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.concordion.api.Element;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Adds custom CSS to the generated specifications
 * @author sumnera
 */
public class ThemeExtension implements ConcordionExtension, SpecificationProcessingListener  {
	private boolean removeConcordionStyle = false;
	private ResourceLocation resourceLocation = ResourceLocation.LINKED;
	private String javaScriptLocation = "js";
	private String cssLocation = "css";
	
	private Resource resource = null;
	
    /**
     * If set to true will remove the default style section for concordion from all specifications.  The CSS file
     * you have supplied will contain a replacement for all concordion styling.  Defaults to false.
     * 
     * @param remove	
     * @return
     */
    public ThemeExtension setRemoveConcordionStyle(boolean removeStyle) {
    	this.removeConcordionStyle = removeStyle;
    	return this;
    }

    /**
     * How should the resources (CSS or JavaScript) be added - Linked or Embedded?  Defaults to Linked.
     * @param resourceLocation
     * @return
     */
    public ThemeExtension setResourceLocation(ResourceLocation resourceLocation) {
    	this.resourceLocation = resourceLocation;
    	return this;
    }
    
    /**
     * By default this extension will add all javascript files from folder "/js" to the spec.
     * 
     * @param javaScriptLocation
     * @return
     */
    public ThemeExtension setJavaScriptLocation(String javaScriptLocation) {
    	this.javaScriptLocation = javaScriptLocation;
    	return this;
    }
    
    /**
     * By default this extension will add all CSS files from folder "/css" to the spec.
     * 
     * @param javaScriptLocation
     * @return
     */
    public ThemeExtension setCSSLocation(String cssLocation) {
    	this.cssLocation = cssLocation;
    	return this;
    }
    
    @Override
    public void addTo(ConcordionExtender concordionExtender) {
        concordionExtender.withSpecificationProcessingListener(this);
        
        if (resourceLocation == ResourceLocation.LINKED) {
	        for (File file : getCSSFiles()) {
	        	concordionExtender.withResource("/" + cssLocation + "/" + file.getName(), new Resource("/" + file.getName()));	
			}	        
	        
	        for (File file : getJavaScriptFiles()) {
	        	concordionExtender.withResource("/" + javaScriptLocation + "/" + file.getName(), new Resource("/" + file.getName()));	
			}
        }
    }
    
	@Override
	public void beforeProcessingSpecification(final SpecificationProcessingEvent event) {
		resource = event.getResource();
	}

	@Override
	public void afterProcessingSpecification(final SpecificationProcessingEvent event) {		
		Element head = event.getRootElement().getFirstChildElement("head");

		removeExistingStyling(head);
		addJavascript(head);			
		addCSS(head);
	}
	
	private void removeExistingStyling(Element head) {
    	// Remove any links to concordion.css created by developers
		List<File> files = getCSSFiles();
		Element[] links = head.getChildElements("link");
		
		for (Element link : links) {
			String href = link.getAttributeValue("href").toLowerCase();
			
			if (href.contains("/concordion.css")) {
				head.removeChild(link);
				continue;
			}
			
			for (File file : files) {
				if (href.contains("/" + file.getName().toLowerCase()) || href.equals(file.getName().toLowerCase())) {
					head.removeChild(link);
					break;
				}
			}
		}
		
		// Remove default concordion styling - if requested
    	if (this.removeConcordionStyle) {
			Element[] styles = head.getChildElements("style");
			for (Element style : styles) {
				if (style.getText().contains(".example")) {
					head.removeChild(style);
				}
			}
    	}
    }
	
	private List<File> getCSSFiles() {
		return getFiles(cssLocation, ".css");
	}

	private List<File> getJavaScriptFiles() {
		return getFiles(javaScriptLocation, ".js");
	}
	
	private List<File> getFiles(String location, String extension) {
		URL url = this.getClass().getClassLoader().getResource(location);
		if (url == null) {
			return new ArrayList<File>();
		}
		
		File folder = new File(url.getPath());
    	if (!folder.isDirectory()) {
    		return new ArrayList<File>();
    	}
    	
    	List<File> files =  new ArrayList<File>(Arrays.asList(folder.listFiles()));
		
		files.removeIf(i -> !i.getName().toLowerCase().endsWith(extension));
				
    	return files;
	}

    private void addCSS(Element head) {
    	List<File> files = getCSSFiles();
    	
    	for (File file : files) {
    		if (resourceLocation == ResourceLocation.LINKED) {
				Resource r = new Resource("/" + file.getName());
						
    			Element link = new Element("link");
    			link.addAttribute("rel", "stylesheet");
    			link.addAttribute("type", "text/css");
    			link.addAttribute("href", resource.getRelativePath(r));
    			
    			head.appendChild(link);
    		} else {
    			String css;
				
    			try {
					css = IOUtils.toString(new FileInputStream(file));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
        	    css = css.replace("\r", "");
    			
    			Element style = new Element("style");    			
    			style.appendText(css);
    			head.appendChild(style);
    		}
    	}
  	}

	private void addJavascript(Element head) {
		List<File> files = getJavaScriptFiles();
    	
    	for (File file : files) {
    		Element script = new Element("script");
			script.addAttribute("type", "text/javascript");
			
			if (resourceLocation == ResourceLocation.LINKED) {
				Resource r = new Resource("/" + file.getName());
						
				script.addAttribute("src", resource.getRelativePath(r));
    			
    		} else {
    			String code;
    			
				try {
					code = IOUtils.toString(new FileInputStream(file));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				code = code.replace("\r", "");
    					
    			script.appendText(code);
    		}
			
			head.appendChild(script);    		
    	}
		
	}
    
    public enum ResourceLocation {
    	EMBEDDED, LINKED
    }
}

