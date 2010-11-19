package org.pentaho.di.ui.core.dialog.geopreview.toolbar;

/**
 * Enumeration of the state that a toolbar could be in
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class ToolbarStates
{
    private static int enumCount = 1;
    private int enumVal;
    private String name;

    
    /**
     * Constructor
     * 
     * @param str String describing the state
     * 
     */
    private ToolbarStates( String str )
    {
      name = str;
      enumVal = enumCount;
      enumCount++;
    }

    
    /**
     * toString methode
     * 
     */
    public String toString() 
    { 
    	return name; 
    }
    /**
     * toInt methode
     * 
     */
    public int toInt() 
    { 
    	return enumVal; 
    }

    // State allowed on a toolbar
    public static final ToolbarStates PAN = new ToolbarStates("Pan");
    public static final ToolbarStates ZOOMIN = new ToolbarStates("ZoomIn");
    public static final ToolbarStates ZOOMOUT = new ToolbarStates("ZoomOut");
    public static final ToolbarStates GETINFOS = new ToolbarStates("Getinfos");
    public static final ToolbarStates NOSELECTION = new ToolbarStates("NoSelection");
}
