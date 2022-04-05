import com.sun.star.uno.UnoRuntime;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.frame.XDesktop;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;

class OpenOfficeClient {
    public static void main(String args[]) {
        String unoUrl = args[0];    // "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager"
        String sourceDir = args[1]; 
        String targetDir = args[2];
        
        XDesktop xDesktop = null;

        try {
            XMultiServiceFactory xmultiservicefactory = com.sun.star.comp.helper.Bootstrap.createSimpleServiceManager();
            Object objectUrlResolver = xmultiservicefactory.createInstance("com.sun.star.bridge.UnoUrlResolver");
            XUnoUrlResolver xurlresolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, objectUrlResolver);

            // open UNO url
            Object objectInitial = xurlresolver.resolve(unoUrl);

            // create factory
            xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);

            Object DesktopInstance = xmultiservicefactory.createInstance("com.sun.star.frame.Desktop");
            xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, DesktopInstance);
            XComponentLoader xCompLoader = (XComponentLoader) UnoRuntime.queryInterface(com.sun.star.frame.XComponentLoader.class, xDesktop);            

            Set<String> dirContents = OpenOfficeClient.listFilesUsingJavaIO(sourceDir);
            for (String file : dirContents) {
                String sourceUrl = "file:///" + sourceDir + file;
                String targetUrl = "file:///" + targetDir + (file.replaceFirst("[.][^.]+$", "")) + ".pdf";
                System.out.println("Processing " + targetUrl);

                // open source file                
                PropertyValue[] propertyValues = new PropertyValue[0];
                propertyValues = new PropertyValue[1];
                propertyValues[0] = new PropertyValue();
                propertyValues[0].Name = "Hidden";
                propertyValues[0].Value = new Boolean(true);
                XComponent xComp = xCompLoader.loadComponentFromURL(sourceUrl, "_blank", 0, propertyValues);

                try {
                    // save as a PDF
                    XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, xComp);
                    propertyValues = new PropertyValue[2];
                    propertyValues[0] = new PropertyValue();
                    propertyValues[0].Name = "FilterName";
                    propertyValues[0].Value = "writer_pdf_Export";
                    propertyValues[1] = new PropertyValue();
                    propertyValues[1].Name = "Overwrite ";
                    propertyValues[1].Value = new Boolean(true);
                    xStorable.storeToURL(targetUrl, propertyValues);
                    
                    System.out.println("Converted " + sourceUrl + " -> " + targetUrl);
                } finally {                    
                    try { 
                        xComp.dispose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }                
            }

            System.out.println("Conversion completed");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (xDesktop != null) {
                try { 
                    // xDesktop.terminate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }
    }

    public static Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
          .filter(file -> !file.isDirectory())
          .map(File::getName)
          .collect(Collectors.toSet());
    }
}
