package ua.leonidius.rtlnotepad;
import java.io.*;

/**
* Objects of this class are used to save all data from editor tabs in onSaveInstanceState().
* Author: Leonidius20
* Project: RTLnotepad
**/

public class TabData implements Serializable
{
	String name;
	File file;
	String text;
	String encoding;
	boolean hasUnsavedChanges;
	boolean isSelected;
	String tag;
}
