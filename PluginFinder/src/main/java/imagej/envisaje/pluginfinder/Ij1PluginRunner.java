package imagej.envisaje.pluginfinder;

import ij.plugin.PlugIn;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;
import imagej.plugin.spi.PluginRunner;

import org.openide.util.lookup.ServiceProvider;

/** Executes an IJ1 plugin. */
@ServiceProvider(service=PluginRunner.class)
public class Ij1PluginRunner implements PluginRunner {

	@Override
	public PlugIn runPlugin(PluginEntry entry) throws PluginException {
		// get Class object for plugin entry
		final ClassLoader loader = ij.IJ.getClassLoader();
		final Class<?> pluginClass;
		try {
			pluginClass = Class.forName(entry.getPluginClass(), true, loader);
		}
		catch (ClassNotFoundException e) {
			throw new PluginException(e);
		}
		if (!PlugIn.class.isAssignableFrom(pluginClass)) {
			throw new PluginException("Not an IJ1 plugin");
		}

		// instantiate plugin
		final Object pluginInstance;
		try {
			pluginInstance = pluginClass.newInstance();
		}
		catch (InstantiationException e) {
			throw new PluginException(e);
		}
		catch (IllegalAccessException e) {
			throw new PluginException(e);
		}
		if (!(pluginInstance instanceof PlugIn)) {
			throw new PluginException("Not an ij.plugin.PlugIn");
		}
		PlugIn plugin = (PlugIn) pluginInstance;

		// execute plugin
		plugin.run(entry.getArg());

	 	// TODO: handle PlugInFilter, ExtendedPlugInFilter (via PlugInFilterRunner?)
		// TODO: use Executer or IJ.runPlugIn instead
		
		return plugin;
	}

}
