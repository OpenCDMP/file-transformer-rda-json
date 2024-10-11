package org.opencdmp.filetransformer.rda.utils.string;

public class MyStringUtils {

	public static int getFirstDifference(String s1, String s2) {
		char[] s1ar = s1.toCharArray();
		char[] s2ar = s2.toCharArray();

		for(int i = 0; i < s1ar.length; i++) {
			if (s2ar.length > i) {
				if (s1ar[i] != s2ar[i]) {
					return i;
				}
			} else {
				return i;
			}
		}

		return -1;
	}
}
