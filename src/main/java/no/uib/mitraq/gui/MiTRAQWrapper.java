package no.uib.mitraq.gui;

import com.compomics.software.CompomicsWrapper;
import java.io.*;

/**
 * A wrapper class used to start the jar file with parameters. The parameters
 * are read from the JavaOptions file in the Properties folder.
 *
 * @author Harald Barsnes
 */
public class MiTRAQWrapper extends CompomicsWrapper {

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     */
    public MiTRAQWrapper() {
        this(null);
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments (ignored if null)
     */
    public MiTRAQWrapper(String[] args) {

        // get the version number set in the pom file
        String jarFileName = "MiTRAQ-" + getVersion() + ".jar";
        String path = this.getClass().getResource("MiTRAQWrapper.class").getPath();
        // remove starting 'file:' tag if there
        if (path.startsWith("file:")) {
            path = path.substring("file:".length(), path.indexOf(jarFileName));
        } else {
            path = path.substring(0, path.indexOf(jarFileName));
        }
        path = path.replace("%20", " ");
        path = path.replace("%5b", "[");
        path = path.replace("%5d", "]");
        File jarFile = new File(path, jarFileName);
        // get the splash 
        String splash = "mitraq-splash.png";
        String mainClass = "no.uib.mitraq.gui.MiTRAQ";

        launchTool("MiTRAQ", jarFile, splash, mainClass, args);
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args
     */
    public static void main(String[] args) {
        new MiTRAQWrapper(args);
    }
    
    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of MiTRAQ
     */
    private String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("mitraq.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("mitraq.version");
    }
}
