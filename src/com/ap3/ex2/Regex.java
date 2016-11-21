import java.util.regex.*;
import java.util.*;

/**
 * class showing how to translate from bash pattern to RegEx pattern
 * and how to then use the resulting Regex pattern to match input from System.in
 */

public class Regex {

	// converts bash pattern to Regex string
	// due to vagaries of parameter expansion on Windows, this code will
	// strip off leading and trailing apostrophes (') from `str'
	//
	// the RegEx string is generated as follows
	// '^' is put at the beginning of the string
	// '*' is converted to ".*"
	// '.' is converted to "\."
	// '?' is converted to "."
	// '$' is put at the end of the string
	
	public static String cvtPattern(String str) {
		StringBuilder pat = new StringBuilder();
		int start, length;

		pat.append('^');
		if (str.charAt(0) == '\'') {	// double quoting on Windows
			start = 1;
			length = str.length() - 1;
		} else {
			start = 0;
			length = str.length();
		}
		for (int i = start; i < length; i++) {
			switch(str.charAt(i)) {
			case '*': pat.append('.'); pat.append('*'); break;
			case '.': pat.append('\\'); pat.append('.'); break;
			case '?': pat.append('.'); break;
			default:  pat.append(str.charAt(i)); break;
			}
		}
		pat.append('$');
		return new String(pat);
	}

	// simple main program - expects bash pattern in Arg[0]
	// after creating the RegEx string and constructing a Pattern,
	// it then attempts to match each line read from System.in against
	// the pattern, printing each matching line on System.out
	
	public static void main(String Arg[]) {
		// convert bash pattern to RegEx pattern, print it out
		String pattern = Regex.cvtPattern(Arg[0]);
		System.out.print(Arg[0] + " --> ");
		System.out.println(pattern);
		// create a scanner to read System.in
		Scanner sc = new Scanner(System.in);
		// compile RegEx pattern into Pattern object
		Pattern p = Pattern.compile(pattern);
		// for each line of input
		while (sc.hasNext()) {
			String line = sc.nextLine();
			// create a matcher against that line of input
			Matcher m = p.matcher(line);
			// if it matches the pattern, print it out
			if (m.matches())
				System.out.println(line);
		}
	}
}
