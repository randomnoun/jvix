package net.sf.jvix.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/** Utility methods used within the VIX API */
public class Util {

	/**
	 * Returns a map of all constants defined in a class. This method
	 * retrieves a list of
	 * all public static final fields with a given field name prefix
	 * (e.g. "OPERATOR_") and returns a map containing constant names to constant values. 
	 * Value objects can be of any type.
	 *
	 * @param constantClass the type-safe enumerated constant class whose values are to be
	 * listed.
	 * @return a list containing the string representations of those values, as returned by
	 * calling <code>toString()</code> on each value.
	 */
	public static Map getConstantsMap(Class clazz, String prefix) {
		if (clazz == null) {
			throw new NullPointerException("class cannot be null");
		}
		if (prefix == null) {
			throw new NullPointerException("prefix cannot be null");
		}

		Map constantMap = new HashMap();
		Object instance;
		Field[] fields = clazz.getDeclaredFields();
		int modifierMask = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

		for (int i = 0; i < fields.length; ++i) {
			Field field = fields[i];
			String name = field.getName();

			if (name.startsWith(prefix) && !name.equals("_revision") && ((field.getModifiers() & modifierMask) == modifierMask)) {
				try {
					instance = field.get(null);
					constantMap.put(name, instance);
				} catch (IllegalAccessException iae) {
					// ok, so they're not public final after all...
					// we can safely ignore them.
				}
			}
		}
		return (Map) constantMap;
	}
}
