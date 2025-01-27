package platformIndependentCore.results;

import javax.swing.JDialog;

// public class ShowWaitAction {

/**
 * Class to display the Checklist Step Checkboxes so user can select Steps to
 * run Automation for
 *
 * @author VBAAUSTAYLOL
 *
 */
@Deprecated
public class ShowWaitAction extends JDialog {

	// /**
	// *
	// */
	private static final long serialVersionUID = -4226742026864148480L;
	// private JProgressBar progressBar;
	// private WaitWorker worker;
	// private JLabel statusLabel = new JLabel("Starting automation");
	// private JLabel progressLabel = new JLabel("( 0 completed)");
	//
	// /**
	// *
	// * @param parent
	// * @param cl
	// * @param testToRun
	// */
	// @SuppressWarnings("unchecked")
	// public ShowWaitAction(File xmlFile, File excelFile) {
	// super();
	// //"Creating " + excelFile.getName() + " Excel Results File ...",
	// DEFAULT_MODALITY_TYPE);
	// JPanel panel = new JPanel();
	// panel.setSize(400, 100);
	// GridBagLayout layout = new GridBagLayout();// new BoxLayout(panel,
	// // BoxLayout.Y_AXIS) ;
	//
	// GridBagConstraints c = new GridBagConstraints();
	// c.ipady = 15;
	// c.ipadx = 15;
	// panel.setLayout(layout);
	// panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
	//
	// setLocation(getLowerRightCornerPoint());
	// repaint();
	//
	// final DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
	// progressBar = new JProgressBar(model);
	// progressBar.setPreferredSize(new Dimension(300, 5));
	// statusLabel
	// .setPreferredSize(new Dimension(300, statusLabel.getHeight()));
	// progressLabel.setPreferredSize(new Dimension(300, progressLabel
	// .getHeight()));
	//
	// progressBar.setMaximumSize(new Dimension(300, 5));
	// statusLabel.setMaximumSize(new Dimension(300, statusLabel.getHeight()));
	// progressLabel.setMaximumSize(new Dimension(300, progressLabel
	// .getHeight()));
	//
	// progressBar.setMinimumSize(new Dimension(300, 5));
	// statusLabel.setMinimumSize(new Dimension(300, statusLabel.getHeight()));
	// progressLabel.setMinimumSize(new Dimension(300, progressLabel
	// .getHeight()));
	//
	// model.setMinimum(0);
	//
	// int max = xmlFile.;
	// if (stepArgs[4] != null) {
	// max = ((ArrayList<String>) stepArgs[4]).size();
	// }
	// model.setMaximum(max);
	//
	// // setLayout(new GridBagLayout());
	// c.fill = GridBagConstraints.HORIZONTAL;
	// c.gridx = 0;
	// c.gridy = 0;
	// panel.add(statusLabel, c);
	// // panel.add(Box.createVerticalGlue());
	// c.gridy = 1;
	// panel.add(progressBar, c);
	// c.gridy = 2;
	// panel.add(progressLabel, c);
	// add(panel);
	//
	// worker = new WaitWorker(xmlFile, excelFile, model);
	// worker.addPropertyChangeListener(new PropertyChangeListener() {
	// @SuppressWarnings("incomplete-switch")
	// @Override
	// public void propertyChange(PropertyChangeEvent evt) {
	// String name = evt.getPropertyName();
	// switch (name) {
	// case "state":
	// switch (worker.getState()) {
	// case DONE:
	// setVisible(false);
	// break;
	// }
	// break;
	// case "progress":
	// // progressBar.setValue(worker.getProgress());
	// break;
	// }
	// }
	// });
	//
	// addWindowListener(new WindowAdapter() {
	//
	// @Override
	// public void windowOpened(WindowEvent e) {
	// worker.execute();
	// }
	//
	// @Override
	// public void windowClosed(WindowEvent e) {
	// worker.cancel(true);
	// }
	//
	// });
	//
	// pack();
	// setLocation(getLowerRightCornerPoint());
	// setAlwaysOnTop(true);
	// setVisible(true);
	//
	// }
	//
	// /**
	// * Will calculate the position for the lower right corner
	// *
	// * @return Point
	// */
	// private Point getLowerRightCornerPoint(){
	// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	// GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
	// Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
	//
	// int x = (int) rect.getMaxX() - this.getWidth();
	// int y = (int) rect.getMaxY() - this.getHeight() - 40;
	//
	// setLocation(x, y);
	// return new Point(x, y);
	//
	// }
	// /**
	// * SwingWorker class used to display Wait popup while the Checklist Steps
	// * are loaded from an ExcelDataFile
	// *
	// * @author VBAAUSTAYLOL
	// *
	// */
	// @SuppressWarnings("rawtypes")
	// public class WaitWorker extends SwingWorker {
	// DefaultBoundedRangeModel model;
	//
	// WaitWorker(File xmlFile, File excelResultsFile,
	// DefaultBoundedRangeModel model) {
	// this.model = model;
	// }
	//
	// @SuppressWarnings("unchecked")
	// protected Object doInBackground() throws Exception {
	//
	//
	// return "Completed";
	//
	// }
	// }

}
