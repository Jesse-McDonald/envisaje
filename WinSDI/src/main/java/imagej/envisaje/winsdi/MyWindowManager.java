
package imagej.envisaje.winsdi;
/*
 * From: http://blogs.sun.com/geertjan/resource/MyWindowManager.java
  */

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.openide.LifecycleManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

/**
 * Trivial window manager that just keeps track of "modes"
 * according to contract but does not really use them, and just opens all
 * top components in their own frames.
 *
 * Useful in case core-windows.jar is not installed, e.g. in standalone usage.
 * @author Jesse Glick
 * @see "#29933"
 *
 */
@SuppressWarnings("deprecation")
//@ServiceProvider(service = WindowManager.class, supersedes = "org.netbeans.core.windows.WindowManagerImpl")
public final class MyWindowManager extends WindowManager {

    public static String MAINWINDOW = "MAINWINDOW";
    private static final boolean VISIBLE = Boolean.parseBoolean(System.getProperty("org.openide.windows.DummyWindowManager.VISIBLE", "true"));
    private static final long serialVersionUID = 1L;
    private static Action[] DEFAULT_ACTIONS_CLONEABLE;
    private static Action[] DEFAULT_ACTIONS_NOT_CLONEABLE;
    private final Map<String, Workspace> workspaces;
    private transient JFrame mw = new JFrame("Main Window");
    private transient PropertyChangeSupport pcs;

    public MyWindowManager() {
        workspaces = new TreeMap<String, Workspace>();
        createWorkspace("default", null).createMode(/*CloneableEditorSupport.EDITOR_MODE*/"editor", "editor", null); // NOI18N

        mw.getContentPane().add(desktop);
        invokeWhenUIReady(new Runnable() {

            public void run() {
                mw.pack();
                mw.setSize(500, 500);
                mw.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent event) {
                        event.getWindow().setVisible(false);
                        event.getWindow().dispose();
                        LifecycleManager.getDefault().saveAll();
                        LifecycleManager.getDefault().exit();

                    }
                });
                mw.setVisible(true);
            }
        });
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }

        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    protected TopComponent.Registry componentRegistry() {
        return new R();
    }

    private R registry() {
        return (R) getRegistry();
    }

    protected WindowManager.Component createTopComponentManager(TopComponent c) {
        return null; // Not used anymore.
    }

    public synchronized Workspace createWorkspace(String name, String displayName) {
        Workspace w = new W(name);
        workspaces.put(name, w);

        if (pcs != null) {
            pcs.firePropertyChange(PROP_WORKSPACES, null, null);
            pcs.firePropertyChange(PROP_CURRENT_WORKSPACE, null, null);
        }

        return w;
    }

    synchronized void delete(Workspace w) {
        workspaces.remove(w.getName());

        if (workspaces.isEmpty()) {
            createWorkspace("default", null); // NOI18N
        }

        if (pcs != null) {
            pcs.firePropertyChange(PROP_WORKSPACES, null, null);
            pcs.firePropertyChange(PROP_CURRENT_WORKSPACE, null, null);
        }
    }

    public synchronized Workspace findWorkspace(String name) {
        return workspaces.get(name);
    }

    public synchronized Workspace getCurrentWorkspace() {
        return workspaces.values().iterator().next();
    }

    public synchronized Workspace[] getWorkspaces() {
        return workspaces.values().toArray(new Workspace[0]);
    }

    public synchronized void setWorkspaces(Workspace[] ws) {
        if (ws.length == 0) {
            throw new IllegalArgumentException();
        }

        workspaces.clear();

        for (int i = 0; i < ws.length; i++) {
            workspaces.put(ws[i].getName(), ws[i]);
        }

        if (pcs != null) {
            pcs.firePropertyChange(PROP_WORKSPACES, null, null);
            pcs.firePropertyChange(PROP_CURRENT_WORKSPACE, null, null);
        }
    }
    private JDesktopPane desktop = new JDesktopPane();

    public synchronized Frame getMainWindow() {
        return mw;
    }

    public void updateUI() {
    }

    // Modes
    public Set<Mode> getModes() {
        Set<Mode> s = new HashSet<Mode>();

        for (Iterator<Workspace> it = new HashSet<Workspace>(workspaces.values()).iterator(); it.hasNext();) {
            Workspace w = it.next();
            s.addAll(w.getModes());
        }

        return s;
    }

    public Mode findMode(TopComponent tc) {
        for (Iterator<Mode> it = getModes().iterator(); it.hasNext();) {
            Mode m = it.next();

            if (Arrays.asList(m.getTopComponents()).contains(tc)) {
                return m;
            }
        }

        return null;
    }

    public Mode findMode(String name) {
        if (name == null) {
            return null;
        }

        for (Iterator<Mode> it = getModes().iterator(); it.hasNext();) {
            Mode m = it.next();

            if (name.equals(m.getName())) {
                return m;
            }
        }

        return null;
    }

    // PENDING Groups not supported.
    public TopComponentGroup findTopComponentGroup(String name) {
        return null;
    }

    //Not supported. Need to access PersistenceManager.
    public TopComponent findTopComponent(String tcID) {
        return null;
    }

    protected String topComponentID(TopComponent tc, String preferredID) {
        return preferredID;
    }

    protected Action[] topComponentDefaultActions(TopComponent tc) {
        // XXX It could be better to provide non-SystemAction instances.
        synchronized (MyWindowManager.class) {
            //Bugfix #33557: Do not provide CloneViewAction when
            //TopComponent does not implement TopComponent.Cloneable
            if (tc instanceof TopComponent.Cloneable) {
                if (DEFAULT_ACTIONS_CLONEABLE == null) {
                    DEFAULT_ACTIONS_CLONEABLE = loadActions(
                            new String[]{"Save", // NOI18N
                                "CloneView", // NOI18N
                                null, "CloseView" // NOI18N
                            });
                }

                return DEFAULT_ACTIONS_CLONEABLE;
            } else {
                if (DEFAULT_ACTIONS_NOT_CLONEABLE == null) {
                    DEFAULT_ACTIONS_NOT_CLONEABLE = loadActions(
                            new String[]{"Save", // NOI18N
                                null, "CloseView" // NOI18N
                            });
                }

                return DEFAULT_ACTIONS_NOT_CLONEABLE;
            }
        }
    }

    private static Action[] loadActions(String[] names) {
        ArrayList<Action> arr = new ArrayList<Action>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (int i = 0; i < names.length; i++) {
            if (names[i] == null) {
                arr.add(null);

                continue;
            }

            try {
                Class<? extends SystemAction> sa = Class.forName("org.openide.actions." + names[i] + "Action", true, loader).asSubclass(SystemAction.class);
                arr.add(SystemAction.get(sa)); // NOI18N
            } catch (ClassNotFoundException e) {
                // ignore it, missing org-openide-actions.jar
            }
        }

        return arr.toArray(new Action[0]);
    }

    protected boolean topComponentIsOpened(TopComponent tc) {
        return tc.isShowing() || registry().opened.contains(tc);
    }

    protected void topComponentActivatedNodesChanged(TopComponent tc, Node[] nodes) {
        registry().setActivatedNodes(tc, nodes);
    }

    protected void topComponentIconChanged(TopComponent tc, Image icon) {
        JInternalFrame f = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, tc);

        if (f != null) {
            f.setFrameIcon(ImageUtilities.image2Icon(icon));
        }
    }

    protected void topComponentToolTipChanged(TopComponent tc, String tooltip) {
        // No op.
    }

    protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
        JInternalFrame f = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, tc);

        if (f != null) {
            f.setTitle(displayName);
        }
    }

    protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
        // no operarion, html looks ugly in frame titles
    }

	@Override
    protected void topComponentOpen(TopComponent tc) {
        JInternalFrame f = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, tc);

        if (f == null) {
            f = new JInternalFrame(tc.getName());
            Image icon = tc.getIcon();
            if (icon != null) {
                f.setFrameIcon(ImageUtilities.image2Icon(icon));
            }
            f.getContentPane().add(tc);
            f.pack();

            f.setVisible(true);
            Boolean main = (Boolean) tc.getClientProperty(MAINWINDOW);
            if (main != null && main) {
                f.setResizable(false);
                try {
                    desktop.add(f);
                    desktop.setComponentZOrder(f, 1);
                    f.setMaximum(true);

                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                desktop.add(f);
                f.setBackground(new Color(255,255,255,64));
                f.setResizable(true);
                f.setClosable(true);
                f.setMaximizable(true);
                f.setIconifiable(true);
                desktop.setComponentZOrder(f, 0);
            }
            final java.lang.ref.WeakReference<TopComponent> ref = new java.lang.ref.WeakReference<TopComponent>(tc);
            f.addInternalFrameListener(
                    new InternalFrameAdapter() {

                        @Override
                        public void internalFrameClosing(InternalFrameEvent e) {
                            TopComponent tc = ref.get();

                            if (tc == null) {
                                return;
                            }

                            tc.close();
                        }

                        @Override
                        public void internalFrameActivated(InternalFrameEvent e) {
                            TopComponent tc = ref.get();

                            if (tc == null) {
                                return;
                            }

                            tc.requestActive();
                        }
                    });
        }

        if (!tc.isShowing()) {
            componentOpenNotify(tc);
            componentShowing(tc);
            if (VISIBLE) {
                f.setVisible(true);
            }
            registry().open(tc);
        }
        if (!mw.isShowing()) {
            mw.setVisible(true);
            mw.setBounds(0, 0, 500, 500);
        }
    }

    protected void topComponentClose(TopComponent tc) {
        if (!tc.canClose()) {
            return;
        }
        componentHidden(tc);
        componentCloseNotify(tc);

        JInternalFrame f = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, tc);

        if (f != null) {
            if (VISIBLE) {
                f.setVisible(false);
            }
            tc.getParent().remove(tc);
        }

        registry().close(tc);

        java.util.Iterator it = workspaces.values().iterator();

        while (it.hasNext()) {
            W w = (W) it.next();
            w.close(tc);
        }
    }

    protected void topComponentRequestVisible(TopComponent tc) {
        JInternalFrame f = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, tc);

        if (f != null) {
            if (VISIBLE) {
                f.setVisible(true);
            }
        }
    }

    protected void topComponentRequestActive(TopComponent tc) {
        JInternalFrame f = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, tc);

        if (f != null) {
            f.toFront();
        }

        registry().setActive(tc);
        activateComponent(tc);
    }

    protected void topComponentRequestAttention(TopComponent tc) {
        //TODO what to do here?
    }

    protected void topComponentCancelRequestAttention(TopComponent tc) {
        //TODO what to do here?
    }

    @Override
    public boolean isEditorTopComponent(TopComponent tc) {
        Mode md = findMode(tc);
        if (md != null && isEditorMode(md)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOpenedEditorTopComponent(TopComponent tc) {
        Mode md = findMode(tc);
        if (md != null && isEditorMode(md)) {
            return tc.isOpened();
        }
        return super.isOpenedEditorTopComponent(tc);
    }

    @Override
    public boolean isEditorMode(Mode mode) {
        return "editor".equals(mode.getName());
    }

    private final class W implements Workspace {

        private static final long serialVersionUID = 1L;
        private final String name;
        private final Map<String, Mode> modes = new HashMap<String, Mode>();
        private final Map<TopComponent, Mode> modesByComponent = new WeakHashMap<TopComponent, Mode>();
        private transient PropertyChangeSupport pcs;

        public W(String name) {
            this.name = name;
        }

        public void activate() {
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener list) {
            if (pcs == null) {
                pcs = new PropertyChangeSupport(this);
            }

            pcs.addPropertyChangeListener(list);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener list) {
            if (pcs != null) {
                pcs.removePropertyChangeListener(list);
            }
        }

        public void remove() {
            MyWindowManager.this.delete(this);
        }

        public synchronized Mode createMode(String name, String displayName, URL icon) {
            Mode m = new M(name);
            modes.put(name, m);

            if (pcs != null) {
                pcs.firePropertyChange(PROP_MODES, null, null);
            }

            return m;
        }

        public synchronized Set<Mode> getModes() {
            return new HashSet<Mode>(modes.values());
        }

        public synchronized Mode findMode(String name) {
            return modes.get(name);
        }

        public synchronized Mode findMode(TopComponent c) {
            return modesByComponent.get(c);
        }

        synchronized void dock(Mode m, TopComponent c) {
            modesByComponent.put(c, m);
        }

        public Rectangle getBounds() {
            return Utilities.getUsableScreenBounds();
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return getName();
        }

        public void close(TopComponent tc) {
            java.util.Iterator it = modes.values().iterator();

            while (it.hasNext()) {
                M m = (M) it.next();
                m.close(tc);
            }
        }

        private final class M implements Mode {

            private static final long serialVersionUID = 1L;
            private final String name;
            private final Set<TopComponent> components = new HashSet<TopComponent>();

            public M(String name) {
                this.name = name;
            }

            public void close(TopComponent tc) {
                components.remove(tc);
            }

            /* Not needed:
            private transient PropertyChangeSupport pcs;
            public synchronized void addPropertyChangeListener(PropertyChangeListener list) {
            if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
            }
            pcs.addPropertyChangeListener(list);
            }
            public synchronized void removePropertyChangeListener(PropertyChangeListener list) {
            if (pcs != null) {
            pcs.removePropertyChangeListener(list);
            }
            }
             */
            public void addPropertyChangeListener(PropertyChangeListener l) {
            }

            public void removePropertyChangeListener(PropertyChangeListener l) {
            }

            public boolean canDock(TopComponent tc) {
                return true;
            }

            public synchronized boolean dockInto(TopComponent c) {
                if (components.add(c)) {
                    Mode old = findMode(c);

                    if ((old != null) && (old != this) && old instanceof M) {
                        synchronized (old) {
                            ((M) old).components.remove(c);
                        }
                    }

                    dock(this, c);
                }

                return true;
            }

            public String getName() {
                return name;
            }

            public String getDisplayName() {
                return getName();
            }

            public Image getIcon() {
                return null;
            }

            public synchronized TopComponent[] getTopComponents() {
                return components.toArray(new TopComponent[0]);
            }

            public Workspace getWorkspace() {
                return W.this;
            }

            public synchronized Rectangle getBounds() {
                return W.this.getBounds();
            }

            public void setBounds(Rectangle s) {
            }

            public TopComponent getSelectedTopComponent() {
                TopComponent[] tcs = components.toArray(new TopComponent[0]);

                return (tcs.length > 0) ? tcs[0] : null;
            }
        }
    }

    private static final class R implements TopComponent.Registry {

        private Reference<TopComponent> active = new WeakReference<TopComponent>(null);
        private final Set<TopComponent> opened;
        private Node[] nodes;
        private PropertyChangeSupport pcs;

        public R() {
            opened = new HashSet<TopComponent>() {

                @Override
                public Iterator<TopComponent> iterator() {
                    HashSet<TopComponent> copy = new HashSet<TopComponent>();
                    Iterator<TopComponent> it = super.iterator();
                    while (it.hasNext()) {
                        TopComponent topComponent = it.next();
                        copy.add(topComponent);
                    }
                    return copy.iterator();
                }
            };
            nodes = new Node[0];
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            if (pcs == null) {
                pcs = new PropertyChangeSupport(this);
            }

            pcs.addPropertyChangeListener(l);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            if (pcs != null) {
                pcs.removePropertyChangeListener(l);
            }
        }

        synchronized void open(TopComponent tc) {
            opened.add(tc);

            if (pcs != null) {
                pcs.firePropertyChange(PROP_TC_OPENED, null, tc);
                pcs.firePropertyChange(PROP_OPENED, null, null);
            }
        }

        synchronized void close(TopComponent tc) {
            opened.remove(tc);

            if (pcs != null) {
                pcs.firePropertyChange(PROP_TC_CLOSED, null, tc);
                pcs.firePropertyChange(PROP_OPENED, null, null);
            }

            if (getActive() == tc) {
                setActive(null);
            }
        }

        public synchronized Set<TopComponent> getOpened() {
            return Collections.unmodifiableSet(opened);
        }

        synchronized void setActive(TopComponent tc) {
            active = new WeakReference<TopComponent>(tc);

            Node[] _nodes = (tc == null) ? new Node[0] : tc.getActivatedNodes();

            if (_nodes != null) {
                nodes = _nodes;

                if (pcs != null) {
                    pcs.firePropertyChange(PROP_ACTIVATED_NODES, null, null);
                }
            }

            if (pcs != null) {
                pcs.firePropertyChange(PROP_ACTIVATED, null, null);
                pcs.firePropertyChange(PROP_CURRENT_NODES, null, null);
            }
        }

        synchronized void setActivatedNodes(TopComponent tc, Node[] _nodes) {
            if (tc == getActive()) {
                if (_nodes != null) {
                    nodes = _nodes;

                    if (pcs != null) {
                        pcs.firePropertyChange(PROP_ACTIVATED_NODES, null, null);
                    }
                }

                if (pcs != null) {
                    pcs.firePropertyChange(PROP_CURRENT_NODES, null, null);
                }
            }
        }

        public TopComponent getActivated() {
            return getActive();
        }

        public Node[] getActivatedNodes() {
            return nodes;
        }

        public synchronized Node[] getCurrentNodes() {
            if (getActive() != null) {
                return getActive().getActivatedNodes();
            } else {
                return null;
            }
        }

        private TopComponent getActive() {
            return active.get();
        }
    }
}
