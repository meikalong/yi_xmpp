package org.test;

import java.lang.reflect.Field;

import org.junit.Test;

public class IntegerTest {
	@Test
	public void test() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> cache = Integer.class.getDeclaredClasses()[0];
		Field mycache = cache.getDeclaredField("cache");
		mycache.setAccessible(true);
		Integer[] newCache = (Integer[]) mycache.get(cache);
		newCache[132] = newCache[133];
		int a = 70, b = a + a;
		System.out.printf("%d + %d = %d", a, a, b);
	}
}
