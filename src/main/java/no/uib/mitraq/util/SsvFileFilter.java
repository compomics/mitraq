package no.uib.mitraq.util;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.ssv files.
 *
 * @author  Harald Barsnes
 */
public class SsvFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.ssv files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        String extension = FileFilterUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(FileFilterUtils.ssv)
                    || extension.equals(FileFilterUtils.SSV)){
                return true;
            } 
            else {
                return false;
            }
        }        
        return false;
    }
    
    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "*.ssv";
    }
}