package test;

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import junit.framework.*;
import aQute.bnd.osgi.*;
import aQute.bnd.service.*;
import aQute.service.reporter.*;
@SuppressWarnings("resource")

public class PluginTest extends TestCase {
	static Processor	main	= new Processor();

	public static void testMissingPluginNotUsed() throws Exception {
		Builder p = new Builder();
		p.setProperty("-plugin", "missing;command:=\"-abc,-def\"");
		/* List<?> plugins = */p.getPlugins(Object.class);
		assertTrue(p.check());

		p.setProperty("-abc", "whatever");
		p.setProperty("-resourceonly", "true");
		p.setProperty("Include-Resource", "jar/osgi.jar");
		p.build();
		assertEquals(1, p.getErrors().size());
		assertTrue(p.getErrors().get(0).contains("Missing plugin"));
	}

	static class TPlugin implements Plugin {
		Map<String,String>	properties;

		@Override
		public void setProperties(Map<String,String> map) {
			properties = map;
		}

		@Override
		public void setReporter(Reporter processor) {
			assertEquals(main, processor);
		}
	}

	public static void testPlugin() {
		main.setProperty(Constants.PLUGIN, "test.PluginTest.TPlugin;a=1;b=2");

		for (TPlugin plugin : main.getPlugins(TPlugin.class)) {
			assertEquals(test.PluginTest.TPlugin.class, plugin.getClass());
			assertEquals("1", plugin.properties.get("a"));
			assertEquals("2", plugin.properties.get("b"));
		}
	}

	public static void testLoadPlugin() {
		main.setProperty(Constants.PLUGIN, "thinlet.Thinlet;path:=jar/thinlet.jar");
		for (Applet applet : main.getPlugins(Applet.class)) {
			assertEquals("thinlet.Thinlet", applet.getClass().getName());
		}
	}

	public static void testLoadPluginFailsWithMissingPath() throws Exception {
		Builder p = new Builder();
		p.setProperty(Constants.PLUGIN, "thinlet.Thinlet");

		p.getPlugins(Object.class);
		assertTrue(p.check("plugin thinlet.Thinlet"));
	}

	public static void testLoadPluginWithPath() {
		Builder p = new Builder();
		p.setProperty(Constants.PLUGIN, "thinlet.Thinlet;path:=jar/thinlet.jar");

		List<MenuContainer> plugins = p.getPlugins(MenuContainer.class);
		assertEquals(0, p.getErrors().size());
		assertEquals(1, plugins.size());
		assertEquals("thinlet.Thinlet", plugins.get(0).getClass().getName());
	}

	public static void testLoadPluginWithGlobalPluginPath() {
		Builder p = new Builder();
		p.setProperty(Constants.PLUGIN, "thinlet.Thinlet");
		p.setProperty(Constants.PLUGINPATH, "jar/thinlet.jar");

		List<MenuContainer> plugins = p.getPlugins(MenuContainer.class);
		assertEquals(0, p.getErrors().size());
		assertEquals(1, plugins.size());
		assertEquals("thinlet.Thinlet", plugins.get(0).getClass().getName());
	}
}
