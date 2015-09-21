package spec.concordion.ext.theme;

import org.concordion.api.extension.Extension;
import org.concordion.ext.ThemeExtension;
import org.concordion.ext.ThemeExtension.ResourceLocation;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import test.concordion.ProcessingResult;

@RunWith(ConcordionRunner.class)
public class Theme extends AcceptanceTest {
	ProcessingResult result = null;
	
	@Extension
    public ThemeExtension theme = new ThemeExtension().setRemoveConcordionStyle(true).setResourceLocation(ResourceLocation.EMBEDDED);

	public boolean testme() {
		return true;
	}
	
	/*
    
    public void setSystemProperty(String name, String value) {
        System.setProperty(name, value);
    }    
    
    public boolean hasStyle(String style) throws Exception {
    	result = getTestRig().processFragment("");
        
        return result.hasLinkedCSS(getBaseOutputDir(), style);
    }
    
    public String getStyle(String style) throws Exception {
    	return result.getLinkedCSS(getBaseOutputDir(), style);
    }
    */
}
