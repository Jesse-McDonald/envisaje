/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.imageframe;

import imagej.dataset.Dataset;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.ImageUtilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//imagej.envisaje.imageframe//ImageDisplay//EN",
autostore = false)
public final class ImageDisplayTopComponent extends TopComponent {

    private static ImageDisplayTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "imagej/envisaje/imageframe/resources/image-x-generic.png";
    private static final String PREFERRED_ID = "ImageDisplayTopComponent";
    private final NavigableImageFrame imgPanel = new NavigableImageFrame();

    public ImageDisplayTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ImageDisplayTopComponent.class, "CTL_ImageDisplayTopComponent"));
        setToolTipText(NbBundle.getMessage(ImageDisplayTopComponent.class, "HINT_ImageDisplayTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        this.add(imgPanel, BorderLayout.CENTER);
    }

    public void setDataset(final Dataset dataset) {

        setName(dataset.getMetaData().getLabel());

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ImageDisplayTopComponent getDefault() {
        if (instance == null) {
            instance = new ImageDisplayTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ImageDisplayTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ImageDisplayTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ImageDisplayTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ImageDisplayTopComponent) {
            return (ImageDisplayTopComponent) win;
        }
        Logger.getLogger(ImageDisplayTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
