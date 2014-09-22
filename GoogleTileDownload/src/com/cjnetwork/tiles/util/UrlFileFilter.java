package com.cjnetwork.tiles.util;

import java.io.File;
import java.io.FileFilter;

public class UrlFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		} else {
			String name = file.getName();
			if (name.endsWith(".txt"))
				return true;
			else
				return false;
		}
	}

}
